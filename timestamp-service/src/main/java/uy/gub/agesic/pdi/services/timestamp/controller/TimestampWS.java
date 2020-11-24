package uy.gub.agesic.pdi.services.timestamp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import uy.gub.agesic.pdi.services.timestamp.business.TimestampBusiness;
import uy.gub.agesic.pdi.services.timestamp.exception.TimeException;

@Endpoint
public class TimestampWS {
    private TimestampBusiness timestampBusiness;

    @Autowired
    public TimestampWS(TimestampBusiness timestampBusiness) {
        this.timestampBusiness = timestampBusiness;
    }

    @PayloadRoot(namespace = "http://www.agesic.gub.uy/soa", localPart = "GetTimestamp")
    @ResponsePayload()
    public GetTimestampResponse getTimestamp() throws TimeException {
        GetTimestampResponse response = new GetTimestampResponse();
        response.setTimeStamp(timestampBusiness.getTimestamp());
        return response;
    }

}
