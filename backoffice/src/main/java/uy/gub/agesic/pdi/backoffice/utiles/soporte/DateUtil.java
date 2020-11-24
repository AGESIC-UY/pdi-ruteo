package uy.gub.agesic.pdi.backoffice.utiles.soporte;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.agesic.pdi.backoffice.utiles.exceptions.BackofficeException;

import java.util.Date;

public class DateUtil {

	private final static Logger logger = LoggerFactory.getLogger(DateUtil.class);

	private static Date NVL_DATE = null; 
	private static Date MIN_DATE = null; 
	private static Date MAX_DATE = null; 

	private static final String DATE_FORMAT = "dd/MM/yyyy";

    private static final String NVL_DATE_DATA = "01/01/0000";
    private static final String MIN_DATE_DATA = "01/01/0001";
    private static final String MAX_DATE_DATA= "31/12/9999";
	static {
		try {
			NVL_DATE = DateUtil.parse(NVL_DATE_DATA, DATE_FORMAT);
			MIN_DATE = DateUtil.parse(MIN_DATE_DATA, DATE_FORMAT);
			MAX_DATE = DateUtil.parse(MAX_DATE_DATA, DATE_FORMAT);
		} catch (BackofficeException e) {
			logger.error("Ha ocurrido un error inicializando las fechas minimas y maximas en la aplicacion", e);
		} 
	}

	public static Date current() {
		return new DateTime().toDate();
	}

    public static Integer currentYear() {
        return new DateTime().getYear();
    }

	public static Date parse(String dateText, String datePattern) throws BackofficeException {
		if (dateText == null || "".equals(dateText)) {
			return null;
		}
		
		try {
			DateTimeFormatter formatter = DateTimeFormat.forPattern(datePattern);
			DateTime date = formatter.parseDateTime(dateText);

			return date.toDate();
		} catch (Throwable ex) {
			logger.error("Error parseando fecha", ex);
			throw new BackofficeException("Error parseando la fecha: " + dateText);
		}
	}

	public static String format(Date date, String datePattern) throws BackofficeException {
		if (date == null) {
			return "";
		}
		
		try {
			DateTimeFormatter formatter = DateTimeFormat.forPattern(datePattern);
			String output = new DateTime(date).toString(formatter);

			return output;
		} catch (Throwable ex) {
			logger.error("Error formateando fecha", ex);
			throw new BackofficeException("Error formateando fecha" + date.toString());
		}
	}

	public static Date addDates(Date date1, Date date2) {
		return new DateTime(date1).plus(new DateTime(date2).getMillis()).toDate();
	}

	public static Date subtractDates(Date date1, Date date2) {
		return new DateTime(date1).minus(new DateTime(date2).getMillis()).toDate();
	}

	public static boolean isBeforeOrEquals(Date date1, Date date2) {
		if (date1 == null && date2 == null)
			return true;
		else if (date1 == null && date2 != null)
			return true;
		else if (date1 != null && date2 == null)
			return false;
		else {
			DateTime dt1 = new DateTime(date1);
			DateTime dt2 = new DateTime(date2);
			
			return dt1.isBefore(dt2) || dt1.isEqual(dt2);
		}
	}
	
	public static boolean isBefore(Date date1, Date date2) {
		if (date1 == null && date2 == null)
			return false;
		else if (date1 == null && date2 != null)
			return true;
		else if (date1 != null && date2 == null)
			return false;
		else {
			DateTime dt1 = new DateTime(date1);
			DateTime dt2 = new DateTime(date2);
			
			return dt1.isBefore(dt2);
		} 
	}

	public static boolean isAfterOrEquals(Date date1, Date date2) {
		if (date1 == null && date2 == null)
			return true;
		else if (date1 == null && date2 != null)
			return false;
		else if (date1 != null && date2 == null)
			return true;
		else {
			DateTime dt1 = new DateTime(date1);
			DateTime dt2 = new DateTime(date2);
			
			return dt1.isAfter(dt2) || dt1.isEqual(dt2);
		}
	}

	
	public static boolean isAfter(Date date1, Date date2) {
		if (date1 == null && date2 == null)
			return false;
		else if (date1 == null && date2 != null)
			return false;
		else if (date1 != null && date2 == null)
			return true;
		else {
			DateTime dt1 = new DateTime(date1);
			DateTime dt2 = new DateTime(date2);
			
			return dt1.isAfter(dt2);
		}
	}

	
	public static boolean isEquals(Date date1, Date date2) {
		if (date1 == null && date2 == null)
			return true;
		else if (date1 == null && date2 != null)
			return false;
		else if (date1 != null && date2 == null)
			return false;
		else 
			return new DateTime(date1).isEqual(new DateTime(date2));
	}

	public static Date getDatePart(Date date) {
		LocalDate dt = new LocalDate(date);
		return dt.toDate();
	}

	public static Date setDatePart(Date date, Date datePart) {
		DateTime dt = new DateTime(date);
		DateTime dtPart = new DateTime(datePart);
		
		MutableDateTime mdate = new MutableDateTime(dt);
		mdate.setDayOfMonth(dtPart.getDayOfMonth());
		mdate.setMonthOfYear(dtPart.getMonthOfYear());
		mdate.setYear(dtPart.getYear());
		
		return mdate.toDateTime().toDate();
	}

	public static String getTimePart(Date date) {
		DateTime dt = new DateTime(date);

		StringBuilder sb = new StringBuilder();

		int hour = dt.getHourOfDay();
		int minute = dt.getMinuteOfHour();
		int second = dt.getSecondOfMinute();

		sb.append(String.valueOf(100 + hour).substring(1));
		sb.append(":");
		sb.append(String.valueOf(100 + minute).substring(1));
		sb.append(":");
		sb.append(String.valueOf(100 + second).substring(1));

		return sb.toString();
	}

	public static Date setTimePart(Date date, String time) {
		DateTime dt = new DateTime(date);
		
		int hour = 0;
		int minute = 0;
		int second = 0;

		if (time != null && !"".equals(time)) {
			String[] parts = time.split(":");
			if (parts.length > 0) {
				try {
					hour = Integer.parseInt(parts[0]);
				} catch (Throwable ex) {}
			}
			if (parts.length > 1) {
				try {
					minute = Integer.parseInt(parts[1]);
				} catch (Throwable ex) {}
			}
			if (parts.length > 2) {
				try {
					second = Integer.parseInt(parts[2]);
				} catch (Throwable ex) {}
			}
		}

		MutableDateTime mdate = new MutableDateTime(dt);
		mdate.setHourOfDay(hour);
		mdate.setMinuteOfHour(minute);
		mdate.setSecondOfMinute(second);

		return mdate.toDateTime().toDate();
	}

	public static Date addDays(Date date, int days) {
		return new DateTime(date).plusDays(days).toDate();
	}

	public static Date subtractDays(Date date, int days) {
		return new DateTime(date).minusDays(days).toDate();
	}

	public static Date nullValue() {
		return NVL_DATE;
	}

	public static Date minimum() {
		return MIN_DATE;
	}

	public static Date maximum() {
		return MAX_DATE;
	}

	public static Date lastDayOfMonth(int month, int year) throws BackofficeException {
		if (month < 1 || month > 12) {
			throw new BackofficeException("Invalid month: " + month);
		}
		
		MutableDateTime mdate = new MutableDateTime();
		mdate.setDayOfMonth(1);
		mdate.setMonthOfYear(month);
		mdate.setYear(year);
		
		DateTime date = mdate.toDateTime().dayOfMonth().withMaximumValue();

		return date.toDate();
	}

	public static Date firstDayOfMonth(int month, int year) throws BackofficeException {
		if (month < 1 || month > 12) {
			throw new BackofficeException("Invalid month: " + month);
		}
		
		MutableDateTime mdate = new MutableDateTime();
		mdate.setDayOfMonth(1);
		mdate.setMonthOfYear(month);
		mdate.setYear(year);
		
		DateTime date = mdate.toDateTime();

		return date.toDate();
	}

	public static int day(Date date) {
		return new DateTime(date).getDayOfMonth();
	}

	public static int month(Date date) {
		return new DateTime(date).getMonthOfYear();
	}

	public static int year(Date date) {
		return new DateTime(date).getYear();
	}

	public static int hours(Date date) {
		return new DateTime(date).getHourOfDay();
	}

	public static int minutes(Date date) {
		return new DateTime(date).getMinuteOfHour();
	}

	public static int seconds(Date date) {
		return new DateTime(date).getSecondOfMinute();
	}

	public static int daysBetween(Date start, Date end) {
		int days = Days.daysBetween(new LocalDate(start), new LocalDate(end)).getDays();
		return days;
	}

	public static long toMilliseconds(Date date) {
		return date.getTime();
	}

	public static Date fromMilliseconds(long milliseconds) {
		return new DateTime(milliseconds).toDate();
	}

	public static Date addMonths(Date date, int months) {
		return new DateTime(date).plusMonths(months).toDate();
	}

	public static Date subtractMonths(Date date, int months) {
		return new DateTime(date).minusMonths(months).toDate();
	}

	public static Date addYears(Date date, int years) {
		return new DateTime(date).plusYears(years).toDate();
	}

	public static Date subtractYears(Date date, int years) {
		return new DateTime(date).minusYears(years).toDate();
	}

	public static Date addMinutes(Date date, int minutes) {
		return new DateTime(date).plusMinutes(minutes).toDate();
	}

	public static Date subtractMinutes(Date date, int minutes) {
		return new DateTime(date).minusMinutes(minutes).toDate();
	}

	public static Date addHours(Date date, int hours) {
		return new DateTime(date).plusHours(hours).toDate();
	}

	public static Date subtractHours(Date date, int hours) {
		return new DateTime(date).minusHours(hours).toDate();
	}

	public static Date addSeconds(Date date, int seconds) {
		return new DateTime(date).plusSeconds(seconds).toDate();
	}

	public static Date subtractSeconds(Date date, int seconds) {
		return new DateTime(date).minusSeconds(seconds).toDate();
	}

	public static boolean isBeforeNow(Date fecha) {
		return new DateTime(fecha).isBeforeNow();
	}

	public static boolean isAfterNow(Date fecha) {
		return new DateTime(fecha).isAfterNow();
	}
}
