package com.yoshio3.services;

import org.springframework.stereotype.Component;

import java.util.Date;

/**
 *
 * @author Yoshio Terada
 */
@Component
public class BlobStorageEntity {
    private String name;
    private String URI;
    private Date lastModifyDate;
    private long size;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the lastModifyDate
     */
    public Date getLastModifyDate() {
        return lastModifyDate;
    }

    /**
     * @param lastModifyDate the lastModifyDate to set
     */
    public void setLastModifyDate(Date lastModifyDate) {
        this.lastModifyDate = lastModifyDate;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * @return the URI
     */
    public String getURI() {
        return URI;
    }

    /**
     * @param URI the URI to set
     */
    public void setURI(String URI) {
        this.URI = URI;
    }
}
