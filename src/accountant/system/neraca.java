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
    private String namaPerusahaan;
    
    public neraca (String namaPersuahaan){
        this.namaPerusahaan = namaPerusahaan;
    }
    public int getId(){
        return id;
    }
    
    public void setId(){
        this.id = id;
    }
    public String getPerusahaan(){
        return namaPerusahaan;
    }
    
    public void  setPerusahaan(){
        this.namaPerusahaan = namaPerusahaan;
    }
    
   
}

