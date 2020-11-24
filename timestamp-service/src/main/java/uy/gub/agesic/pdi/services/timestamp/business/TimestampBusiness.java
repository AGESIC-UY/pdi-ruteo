package uy.gub.agesic.pdi.services.timestamp.business;

import uy.gub.agesic.pdi.services.timestamp.exception.TimeException;

import java.util.Calendar;

public interface TimestampBusiness {

    Calendar getTimestamp() throws TimeException;

}
