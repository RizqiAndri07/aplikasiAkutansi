package accountant.system;

import java.time.LocalDate;

public class Jurnal {

    private final int id;
    private final int kodeAkun; // Tambahan atribut untuk kode akun
    private final LocalDate tanggal;
    private final String deskripsi;
    private final int totalTransaksi;
    private final String linkDokumen;

    public Jurnal(int id, int kodeAkun, LocalDate tanggal, String deskripsi, int totalTransaksi, String linkDokumen) {
        this.id = id;
        this.kodeAkun = kodeAkun;
        this.tanggal = tanggal;
        this.deskripsi = deskripsi;
        this.totalTransaksi = totalTransaksi;
        this.linkDokumen = linkDokumen;
    }

    public int getId() {
        return id;
    }

    public int getKodeAkun() {
        return kodeAkun;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public int getTotalTransaksi() {
        return totalTransaksi;
    }

    public String getLinkDokumen() {
        return linkDokumen;
    }
}
