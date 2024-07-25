/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package accountant.system;

/**
 *
 * @author rizqi
 */
public class neraca {
    private int id;
    private final String labelAktifaLancar;
    private final int valueAktifaLancar;
    private final String labelAktifaTetap;
    private final int valueAktifaTetap;
    
    public neraca (String labelAktifaLancar,int valueAktifaLancar,String labelAktifaTetap,int valueAktifaTetap){
        this.labelAktifaLancar = labelAktifaLancar;
        this.valueAktifaLancar = valueAktifaLancar;
        this.labelAktifaTetap = labelAktifaTetap;
        this.valueAktifaTetap = valueAktifaTetap;
    }
    public int getId(){
        return id;
    }
    
    public String getLabelAktifaLancar(){
        return labelAktifaLancar;
    }
    public int getValueAktifaLancar(){
        return valueAktifaLancar;
    }
    public String getLabelAktifaTetap(){
        return labelAktifaTetap;
    }
    public int getvalueAktifaTetap(){
        return valueAktifaTetap;
    }
    
   
}

