/*
 * $Id: CountsCalibrator.java,v 1.1 2006/09/12 11:42:42 marcop Exp $
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
package org.esa.beam.dataio.avhrr.calibration;

import org.esa.beam.dataio.avhrr.AvhrrConstants;
import org.esa.beam.dataio.avhrr.AvhrrReader;


/**
 * This calibrator returns counts and completely ignores calibration data.
 */
public class CountsCalibrator extends AbstractCalibrator {

    public CountsCalibrator(int channel) {
        super(channel);
    }

    @Override
    public String getBandName() {
        return AvhrrConstants.COUNTS_BAND_NAME_PREFIX + AvhrrConstants.CH_STRINGS[channel];
    }

    @Override
    public String getBandUnit() {
        return AvhrrConstants.COUNTS_UNIT;
    }

    @Override
    public String getBandDescription() {
        return AvhrrReader.format(AvhrrConstants.COUNTS_DESCRIPTION, AvhrrConstants.CH_STRINGS[channel]);
    }

    @Override
    public boolean requiresCalibrationData() {
        return false;
    }

    @Override
    public boolean processCalibrationData(int[] calibrationData) {
        return true;
    }

    @Override
    public float calibrate(int counts) {
        return counts;
    }
}

