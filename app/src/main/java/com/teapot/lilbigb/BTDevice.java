package com.teapot.lilbigb;



/**
 * Created by zeb on 07-10-15.
 */
public class BTDevice {

    private String name;
    private String adr;
    private String contact;

    public BTDevice(String name, String adr, String contact){
        this.name = name;
        this.adr = adr;
        this.contact = contact;
    }

    public String getName(){ return name; }
    public String getAdr(){ return adr; }
    public String getContact(){ return contact; }

}
