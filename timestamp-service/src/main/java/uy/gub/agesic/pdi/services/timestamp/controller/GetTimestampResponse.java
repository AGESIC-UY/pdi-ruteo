package uy.gub.agesic.pdi.services.timestamp.controller;

import javax.xml.bind.annotation.*;
import java.util.Calendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "timeStamp"
})
@XmlRootElement(name = "GetTimestampResponse", namespace = "http://www.agesic.gub.uy/soa")
public class GetTimestampResponse {

    @XmlElement(name = "Timestamp")
    private Calendar timeStamp;

    public Calendar getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Calendar timeStamp) {
        this.timeStamp = timeStamp;
    }

}
