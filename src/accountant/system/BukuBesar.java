package accountant.system;

import java.time.LocalDate;

public class BukuBesar {
    private final LocalDate tanggal;
    private final String deskripsi;
    private final int kredit;
    private final int debit;
    private final int saldo;

    public BukuBesar(LocalDate tanggal, String deskripsi, int kredit, int debit, int saldo) {
        this.tanggal = tanggal;
        this.deskripsi = deskripsi;
        this.kredit = kredit;
        this.debit = debit;
        this.saldo = saldo;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public int getKredit() {
        return kredit;
    }

    public int getDebit() {
        return debit;
    }

    public int getSaldo() {
        return saldo;
    }
}
