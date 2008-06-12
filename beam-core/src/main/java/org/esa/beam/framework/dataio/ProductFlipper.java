/*
 * $Id: ProductFlipper.java,v 1.5 2007/03/19 15:52:27 marcop Exp $
 *
 * Copyright (C) 2002 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.framework.dataio;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.SubProgressMonitor;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.FlagCoding;
import org.esa.beam.framework.datamodel.ImageInfo;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.datamodel.TiePointGeoCoding;
import org.esa.beam.framework.datamodel.TiePointGrid;
import org.esa.beam.framework.dataop.maptransf.Datum;
import org.esa.beam.util.Debug;
import org.esa.beam.util.Guardian;

import java.io.IOException;

/**
 * @author Norman Fomferra
 * @version $Revision$ $Date$
 */
public class ProductFlipper extends AbstractProductBuilder {

    public final static int FLIP_HORIZONTAL = 1;
    public final static int FLIP_VERTICAL = 2;
    public final static int FLIP_BOTH = 3;

    private int _flipType;

    public ProductFlipper(int flipType) {
        this(flipType, false);
    }

    public ProductFlipper(int flipType, boolean sourceProductOwner) {
        super(sourceProductOwner);
        if ((flipType != FLIP_HORIZONTAL) && (flipType != FLIP_VERTICAL) && (flipType != FLIP_BOTH)) {
            throw new IllegalArgumentException("invalid flip type");
        }
        _flipType = flipType;
    }

    public static Product createFlippedProduct(Product sourceProduct, int flipType, String name, String desc) throws
                                                                                                              IOException {
        return createFlippedProduct(sourceProduct, false, flipType, name, desc);
    }

    public static Product createFlippedProduct(Product sourceProduct, boolean sourceProductOwner, int flipType,
                                               String name, String desc) throws IOException {
        ProductFlipper productFlipper = new ProductFlipper(flipType, sourceProductOwner);
        return productFlipper.readProductNodes(sourceProduct, null, name, desc);
    }

    public int getFlipType() {
        return _flipType;
    }

    /**
     * Reads a data product and returns a in-memory representation of it. This method was called by
     * <code>readProductNodes(input, subsetInfo)</code> of the abstract superclass.
     *
     * @throws IllegalArgumentException if <code>input</code> type is not one of the supported input sources.
     * @throws IOException              if an I/O error occurs
     */
    @Override
    protected Product readProductNodesImpl() throws IOException {
        if (getInput() instanceof Product) {
            _sourceProduct = (Product) getInput();
        } else {
            throw new IllegalArgumentException("unsupported input source: " + getInput());
        }
        if (_flipType == 0) {
            throw new IllegalStateException("no flip type set");
        }

        _sceneRasterWidth = _sourceProduct.getSceneRasterWidth();
        _sceneRasterHeight = _sourceProduct.getSceneRasterHeight();

        return createProduct();
    }

    /**
     * Closes the access to all currently opened resources such as file input streams and all resources of this children
     * directly owned by this reader. Its primary use is to allow the garbage collector to perform a vanilla job.
     * <p/>
     * <p>This method should be called only if it is for sure that this object instance will never be used again. The
     * results of referencing an instance of this class after a call to <code>close()</code> are undefined.
     * <p/>
     * <p>Overrides of this method should always call <code>super.close();</code> after disposing this instance.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        disposeBandMap();
        _sourceProduct = null;
        super.close();
    }

    /**
     * Reads raster data from the data source specified by the given destination band into the given in-memory buffer
     * and region.
     * <p/>
     * <p>For a complete description, please refer to the {@link ProductReader#readBandRasterData(org.esa.beam.framework.datamodel.Band, int, int, int, int, org.esa.beam.framework.datamodel.ProductData, com.bc.ceres.core.ProgressMonitor)}  interface definition}
     * of this method.
     * <p/>
     * <p>The <code>AbstractProductReader</code> implements this method using the <i>Template Method</i> pattern. The
     * template method in this case is the abstract method to which the call is delegated after an optional spatial
     * subset given by {@link #getSubsetDef()} has been applied to the input parameters.
     *
     * @param destBand    the destination band which identifies the data source from which to read the sample values
     * @param destOffsetX the X-offset in the band's raster co-ordinates
     * @param destOffsetY the Y-offset in the band's raster co-ordinates
     * @param destWidth   the width of region to be read given in the band's raster co-ordinates
     * @param destHeight  the height of region to be read given in the band's raster co-ordinates
     * @param destBuffer  the destination buffer which receives the sample values to be read
     * @param pm          a monitor to inform the user about progress
     *
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if the number of elements destination buffer not equals <code>destWidth *
     *                                  destHeight</code> or the destination region is out of the band's raster
     * @see #readBandRasterDataImpl
     * @see #getSubsetDef()
     * @see ProductReader#readBandRasterData(org.esa.beam.framework.datamodel.Band, int, int, int, int, org.esa.beam.framework.datamodel.ProductData, com.bc.ceres.core.ProgressMonitor)
     * @see org.esa.beam.framework.datamodel.Band#getRasterWidth()
     * @see org.esa.beam.framework.datamodel.Band#getRasterHeight()
     */
    @Override
    public void readBandRasterData(Band destBand,
                                   int destOffsetX,
                                   int destOffsetY,
                                   int destWidth,
                                   int destHeight,
                                   ProductData destBuffer,
                                   ProgressMonitor pm) throws IOException {

        Band sourceBand = (Band) _bandMap.get(destBand);
        Debug.assertNotNull(sourceBand);

        Guardian.assertNotNull("destBand", destBand);
        Guardian.assertNotNull("destBuffer", destBuffer);

        if (destBuffer.getNumElems() < destWidth * destHeight) {
            throw new IllegalArgumentException("destination buffer too small");
        }
        if (destBuffer.getNumElems() > destWidth * destHeight) {
            throw new IllegalArgumentException("destination buffer too big");
        }


        final int sourceW = _sourceProduct.getSceneRasterWidth();
        final int sourceH = _sourceProduct.getSceneRasterHeight();

        int sourceX;
        int sourceY;

        float[] line = new float[sourceW];

        pm.beginTask("Flipping raster data...", destHeight);
        try {
            if (_flipType == FLIP_HORIZONTAL) {
                for (int j = 0; j < destHeight; j++) {
                    if (pm.isCanceled()) {
                        break;
                    }
                    sourceY = destOffsetY + j;
                    sourceBand.readPixels(0, sourceY, sourceW, 1, line, SubProgressMonitor.create(pm, 1));
                    for (int i = 0; i < destWidth; i++) {
                        sourceX = sourceW - (destOffsetX + i + 1);
                        destBuffer.setElemFloatAt(j * destWidth + i, line[sourceX]);
                    }
                }
            } else if (_flipType == FLIP_VERTICAL) {
                for (int j = 0; j < destHeight; j++) {
                    if (pm.isCanceled()) {
                        break;
                    }
                    sourceY = sourceH - (destOffsetY + j + 1);
                    sourceBand.readPixels(0, sourceY, sourceW, 1, line, SubProgressMonitor.create(pm, 1));
                    for (int i = 0; i < destWidth; i++) {
                        sourceX = destOffsetX + i;
                        destBuffer.setElemFloatAt(j * destWidth + i, line[sourceX]);
                    }
                }
            } else {
                for (int j = 0; j < destHeight; j++) {
                    if (pm.isCanceled()) {
                        break;
                    }
                    sourceY = sourceH - (destOffsetY + j + 1);
                    sourceBand.readPixels(0, sourceY, sourceW, 1, line, SubProgressMonitor.create(pm, 1));
                    for (int i = 0; i < destWidth; i++) {
                        sourceX = sourceW - (destOffsetX + i + 1);
                        destBuffer.setElemFloatAt(j * destWidth + i, line[sourceX]);
                    }
                }
            }
        } finally {
            pm.done();
        }
    }


    /**
     * The template method which is called by the <code>readBandRasterDataSubSampling</code> method after an optional
     * spatial subset has been applied to the input parameters.
     * <p/>
     * <p>The destination band, buffer and region parameters are exactly the ones passed to the original
     * <code>readBandRasterDataSubSampling</code> call. Since the <code>destOffsetX</code> and <code>destOffsetY</code>
     * parameters are already taken into acount in the <code>sourceOffsetX</code> and <code>sourceOffsetY</code>
     * parameters, an implementor of this method is free to ignore them.
     *
     * @param sourceOffsetX the absolute X-offset in source raster co-ordinates
     * @param sourceOffsetY the absolute Y-offset in source raster co-ordinates
     * @param sourceWidth   the width of region providing samples to be read given in source raster co-ordinates
     * @param sourceHeight  the height of region providing samples to be read given in source raster co-ordinates
     * @param sourceStepX   the sub-sampling in X direction within the region providing samples to be read
     * @param sourceStepY   the sub-sampling in Y direction within the region providing samples to be read
     * @param destBand      the destination band which identifies the data source from which to read the sample values
     * @param destOffsetX   the X-offset in the band's raster co-ordinates
     * @param destOffsetY   the Y-offset in the band's raster co-ordinates
     * @param destWidth     the width of region to be read given in the band's raster co-ordinates
     * @param destHeight    the height of region to be read given in the band's raster co-ordinates
     * @param destBuffer    the destination buffer which receives the sample values to be read
     * @param pm            a monitor to inform the user about progress
     *
     * @throws IOException if an I/O error occurs
     * @see #getSubsetDef
     */
    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX,
                                          int sourceOffsetY,
                                          int sourceWidth,
                                          int sourceHeight,
                                          int sourceStepX,
                                          int sourceStepY,
                                          Band destBand,
                                          int destOffsetX,
                                          int destOffsetY,
                                          int destWidth,
                                          int destHeight,
                                          ProductData destBuffer,
                                          ProgressMonitor pm) throws IOException {
        throw new IllegalStateException("invalid call");
    }

    private Product createProduct() {
        Debug.assertNotNull(getSourceProduct());
        Debug.assertTrue(getSceneRasterWidth() > 0);
        Debug.assertTrue(getSceneRasterHeight() > 0);
        final String newProductName;
        if (_newProductName == null || _newProductName.length() == 0) {
            newProductName = getSourceProduct().getName();
        } else {
            newProductName = _newProductName;
        }
        final Product product = new Product(newProductName, getSourceProduct().getProductType(),
                                            getSceneRasterWidth(),
                                            getSceneRasterHeight(),
                                            this);
        product.setPointingFactory(getSourceProduct().getPointingFactory());
        if (_newProductDesc == null || _newProductDesc.length() == 0) {
            product.setDescription(getSourceProduct().getDescription());
        } else {
            product.setDescription(_newProductDesc);
        }
        if (!isMetadataIgnored()) {
            addMetadataToProduct(product);
            addTiePointGridsToProduct(product);
            addFlagCodingsToProduct(product);
            addGeoCodingToProduct(product);
        }
        addBandsToProduct(product);
        addBitmaskDefsToProduct(product);
        return product;
    }

    // @todo 1 nf/nf - duplicated code in ProductProjectionBuilder, ProductFlipper and ProductSubsetBulider
    private void addBandsToProduct(Product product) {
        Debug.assertNotNull(getSourceProduct());
        Debug.assertNotNull(product);
        for (int i = 0; i < getSourceProduct().getNumBands(); i++) {
            Band sourceBand = getSourceProduct().getBandAt(i);
            String bandName = sourceBand.getName();
            if (isNodeAccepted(bandName)) {
                Band destBand;
                if (sourceBand.isScalingApplied()) {
                    destBand = new Band(bandName,
                                        ProductData.TYPE_FLOAT32,
                                        getSceneRasterWidth(),
                                        getSceneRasterHeight());
                } else {
                    destBand = new Band(bandName,
                                        sourceBand.getDataType(),
                                        getSceneRasterWidth(),
                                        getSceneRasterHeight());
                }
                if (sourceBand.getUnit() != null) {
                    destBand.setUnit(sourceBand.getUnit());
                }
                if (sourceBand.getDescription() != null) {
                    destBand.setDescription(sourceBand.getDescription());
                }
                destBand.setSpectralBandIndex(sourceBand.getSpectralBandIndex());
                destBand.setSpectralWavelength(sourceBand.getSpectralWavelength());
                destBand.setSpectralBandwidth(sourceBand.getSpectralBandwidth());
                destBand.setSolarFlux(sourceBand.getSolarFlux());
                FlagCoding sourceFlagCoding = sourceBand.getFlagCoding();
                if (sourceFlagCoding != null) {
                    String flagCodingName = sourceFlagCoding.getName();
                    FlagCoding destFlagCoding = product.getFlagCoding(flagCodingName);
                    Debug.assertNotNull(
                            destFlagCoding); // should not happen because flag codings should be already in product
                    destBand.setFlagCoding(destFlagCoding);
                } else {
                    destBand.setFlagCoding(null);
                }
                ImageInfo sourceImageInfo = sourceBand.getImageInfo();
                if (sourceImageInfo != null) {
                    destBand.setImageInfo(sourceImageInfo.createDeepCopy());
                }
                product.addBand(destBand);
                _bandMap.put(destBand, sourceBand);
            }
        }
    }

    private void addTiePointGridsToProduct(final Product product) {
        int sourceOffsetX = 0;
        int sourceOffsetY = 0;
        int sourceStepX = 1;
        int sourceStepY = 1;
        if (getSubsetDef() != null) {
            sourceStepX = getSubsetDef().getSubSamplingX();
            sourceStepY = getSubsetDef().getSubSamplingY();
            if (getSubsetDef().getRegion() != null) {
                sourceOffsetX = getSubsetDef().getRegion().x;
                sourceOffsetY = getSubsetDef().getRegion().y;
            }
        }

        for (int i = 0; i < getSourceProduct().getNumTiePointGrids(); i++) {
            final TiePointGrid sourceTiePointGrid = getSourceProduct().getTiePointGridAt(i);
            if (isNodeAccepted(sourceTiePointGrid.getName())) {

                final float[] sourcePoints = sourceTiePointGrid.getTiePoints();
                final float[] targetPoints = new float[sourcePoints.length];
                final int width = sourceTiePointGrid.getRasterWidth();
                final int height = sourceTiePointGrid.getRasterHeight();

                if (_flipType == FLIP_HORIZONTAL) {
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            targetPoints[x + y * width] = sourcePoints[width - x - 1 + y * width];
                        }
                    }
                } else if (_flipType == FLIP_VERTICAL) {
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            targetPoints[x + y * width] = sourcePoints[x + (height - y - 1) * width];
                        }
                    }
                } else {
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            targetPoints[x + y * width] = sourcePoints[width - x - 1 + (height - y - 1) * width];
                        }
                    }
                }
                final TiePointGrid tiePointGrid = new TiePointGrid(sourceTiePointGrid.getName(),
                                                                   sourceTiePointGrid.getRasterWidth(),
                                                                   sourceTiePointGrid.getRasterHeight(),
                                                                   sourceTiePointGrid.getOffsetX() - (float) sourceOffsetX,
                                                                   sourceTiePointGrid.getOffsetY() - (float) sourceOffsetY,
                                                                   sourceTiePointGrid.getSubSamplingX() / (float) sourceStepX,
                                                                   sourceTiePointGrid.getSubSamplingY() / (float) sourceStepY,
                                                                   targetPoints,
                                                                   sourceTiePointGrid.getDiscontinuity());
                tiePointGrid.setUnit(sourceTiePointGrid.getUnit());
                tiePointGrid.setDescription(sourceTiePointGrid.getDescription());
                product.addTiePointGrid(tiePointGrid);
            }
        }
    }

    private static void addGeoCodingToProduct(final Product product) {
        TiePointGrid latGrid = product.getTiePointGrid("latitude");
        TiePointGrid lonGrid = product.getTiePointGrid("longitude");
        if (latGrid != null && lonGrid != null) {
            product.setGeoCoding(new TiePointGeoCoding(latGrid, lonGrid, Datum.WGS_84));
        }

    }
}
