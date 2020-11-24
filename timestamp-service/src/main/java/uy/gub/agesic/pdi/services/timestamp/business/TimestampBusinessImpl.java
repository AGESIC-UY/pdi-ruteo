package uy.gub.agesic.pdi.services.timestamp.business;

import org.springframework.stereotype.Service;
import uy.gub.agesic.pdi.services.timestamp.exception.TimeException;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

@Service
public class TimestampBusinessImpl implements TimestampBusiness {

    public Calendar getTimestamp() throws TimeException {
        return GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
    }
}
