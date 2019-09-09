package org.esa.snap.remote.products.repository.tao.scihub;

import org.esa.snap.remote.products.repository.RepositoryProduct;
import org.esa.snap.remote.products.repository.tao.AbstractTAORemoteRepositoryProvider;
import org.esa.snap.remote.products.repository.ProductRepositoryDownloader;
import ro.cs.tao.datasource.remote.ProductHelper;
import ro.cs.tao.datasource.remote.scihub.SciHubDataSource;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.products.sentinels.SentinelProductHelper;

/**
 * Created by jcoravu on 26/8/2019.
 */
public class SciHubProductsRepositoryProvider extends AbstractTAORemoteRepositoryProvider<SciHubDataSource> {

    public SciHubProductsRepositoryProvider() {
    }

    @Override
    protected SciHubRepositoryProduct buildRepositoryProduct(EOProduct product, String mission) {
        return new SciHubRepositoryProduct(product, mission);
    }

    @Override
    protected ProductHelper buildProductHelper(String productName) {
        return SentinelProductHelper.create(productName);
    }

    @Override
    protected Class<SciHubDataSource> getDataSourceClass() {
        return SciHubDataSource.class;
    }

    @Override
    public String getRepositoryName() {
        return "ESA SciHub";
    }

    @Override
    public ProductRepositoryDownloader buidProductDownloader(String mission) {
        return new SciHubProductRepositoryDownloader(mission, getRepositoryId());
    }
}
