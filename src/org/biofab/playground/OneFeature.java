/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biofab.playground;

/**
 *
 * @author juul
 */
public class OneFeature {

    String design_id;
    String dbid;
    String seq;
    Integer start;
    Integer end;

    public OneFeature(String _design_id, String _dbid, String _seq, Integer _start, Integer _end) {
        design_id = _design_id;
        dbid = _dbid;
        seq = _seq;
        start = _start;
        end = _end;
    }


    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public String getDbid() {
        return dbid;
    }

    public void setDbid(String dbid) {
        this.dbid = dbid;
    }

    public String getDesign_id() {
        return design_id;
    }

    public void setDesign_id(String design_id) {
        this.design_id = design_id;
    }


    
}
