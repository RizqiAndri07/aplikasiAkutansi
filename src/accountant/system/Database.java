package accountant.system;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class Database {

    private static final String URL = "jdbc:sqlite:accountant.db";
    private static final String PREF_KEY = "databaseInitialized";
    private static final Preferences prefs = Preferences.userNodeForPackage(Database.class);

    public static Connection connect() {
        Connection conn = null;
        try {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
            conn = DriverManager.getConnection(URL);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    private static void createTables() {
        // SQL statement for creating new tables
        String sqlUser = "CREATE TABLE IF NOT EXISTS user ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nama_perusahaan VARCHAR(100) NOT NULL,"
                + " alamat_perusahaan TEXT NOT NULL,"
                + " email VARCHAR(100) NOT NULL,"
                + " no_hp VARCHAR(20) NOT NULL,"
                + " jenis_usaha VARCHAR(50) NOT NULL,"
                + " nama_pemilik VARCHAR(100) NOT NULL,"
                + " no_npwp VARCHAR(50) NOT NULL,"
                + " logo_perusahaan TEXT,"
                + " username VARCHAR(50) NOT NULL UNIQUE,"
                + " password VARCHAR(255) NOT NULL,"
                + " created_at DATE,"
                + " reset_token TEXT"
                + ");";

        String sqlAkunLevel1 = "CREATE TABLE IF NOT EXISTS akun_1 ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nama VARCHAR(255) NOT NULL"
                + ");";

        String sqlAkunLevel2 = "CREATE TABLE IF NOT EXISTS akun_2 ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nama VARCHAR(255) NOT NULL,"
                + " id_akun1 INTEGER NOT NULL,"
                + " FOREIGN KEY(id_akun1) REFERENCES akun_1(id)"
                + ");";

        String sqlAkunLevel3 = "CREATE TABLE IF NOT EXISTS akun_3 ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nama VARCHAR(255) NOT NULL,"
                + " definisi TEXT NOT NULL,"
                + " id_akun2 INTEGER NOT NULL,"
                + " FOREIGN KEY(id_akun2) REFERENCES akun_2(id)"
                + ");";

        String sqlJurnal = "CREATE TABLE IF NOT EXISTS jurnal ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " tanggal DATE NOT NULL,"
                + " deskripsi TEXT NOT NULL,"
                + " total_transaksi INTEGER NOT NULL,"
                + " link TEXT NOT NULL,"
                + " user_id INTEGER NOT NULL,"
                + " FOREIGN KEY(user_id) REFERENCES user(id)"
                + ");";

        String sqlDetailJurnal = "CREATE TABLE IF NOT EXISTS detail_jurnal ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " jurnal_id INTEGER NOT NULL,"
                + " akun_id INTEGER NOT NULL,"
                + " debit INTEGER NOT NULL DEFAULT 0,"
                + " kredit INTEGER NOT NULL DEFAULT 0,"
                + " FOREIGN KEY (jurnal_id) REFERENCES Jurnal(id),"
                + " FOREIGN KEY(akun_id) REFERENCES akun_3(id)"
                + ");";

        String sqlBukuBesar = "CREATE TABLE IF NOT EXISTS buku_besar ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " tanggal DATE NOT NULL,"
                + " akun_id INTEGER NOT NULL,"
                + " deskripsi TEXT NOT NULL,"
                + " debit INTEGER NOT NULL DEFAULT 0,"
                + " kredit INTEGER NOT NULL DEFAULT 0,"
                + " saldo INTEGER NOT NULL,"
                + " user_id INTEGER NOT NULL,"
                + " FOREIGN KEY(akun_id) REFERENCES akun_3(id),"
                + " FOREIGN KEY(user_id) REFERENCES user(id)"
                + ");";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUser);
            stmt.execute(sqlAkunLevel1);
            stmt.execute(sqlAkunLevel2);
            stmt.execute(sqlAkunLevel3);
            stmt.execute(sqlJurnal);
            stmt.execute(sqlDetailJurnal);
            stmt.execute(sqlBukuBesar);

            // Set initial id value for 'user' table to 101
            String sqlAlterTable = "INSERT INTO sqlite_sequence (name, seq) VALUES ('user', 100);";
            stmt.execute(sqlAlterTable);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Setup Database (**ONLY FIRST TIME**)
    static void setupDatabase() {
        boolean isDatabaseInitialized = prefs.getBoolean(PREF_KEY, false);

        if (!isDatabaseInitialized) {
            try (Connection conn = DriverManager.getConnection(URL)) {
                createTables();
                insertAkunLevel1(conn);
                insertAkunLevel2(conn);
                insertAkunLevel3(conn);
                System.out.println("Database setup successfully!...");

                // Mark the database as initialized
                prefs.putBoolean(PREF_KEY, true);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Database already initialized. Skipping setup.");
        }
    }

    static void hapusDB() {
        prefs.remove(PREF_KEY);
    }

    private static void resetDB() {
        String sql = "DELETE FROM user";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("User data deleted successfully!");
        } catch (SQLException e) {
            System.err.println("Error deleting user data: " + e.getMessage());
        }
    }

    // Method to insert data into akun_1
    private static void insertAkunLevel1(Connection conn) throws SQLException {
        String sql = "INSERT INTO akun_1 (id, nama) VALUES (?, ?)";
        List<Object[]> data = List.of(
                new Object[]{1, "Aset"},
                new Object[]{2, "Hutang"},
                new Object[]{3, "Modal"},
                new Object[]{4, "Pendapatan"},
                new Object[]{5, "Beban"}
        );

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] entry : data) {
                pstmt.setInt(1, (int) entry[0]);
                pstmt.setString(2, (String) entry[1]);
                pstmt.executeUpdate();
            }
        }
    }

    // Method to insert data into akun_2
    private static void insertAkunLevel2(Connection conn) throws SQLException {
        String sql = "INSERT INTO akun_2 (id, nama, id_akun1) VALUES (?, ?, ?)";
        List<Object[]> data = List.of(
                new Object[]{1, "Aset Lancar", 1},
                new Object[]{2, "Aset Tetap", 1},
                new Object[]{3, "Akumulasi Penyusutan", 1},
                new Object[]{4, "Hutang Jangka Pendek", 2},
                new Object[]{5, "Hutang Jangka Panjang", 2},
                new Object[]{6, "Modal", 3},
                new Object[]{7, "Pendapatan", 4},
                new Object[]{8, "Beban Usaha", 5},
                new Object[]{9, "Beban Penjualan", 5},
                new Object[]{10, "Beban Umum", 5}
        );

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] entry : data) {
                pstmt.setInt(1, (int) entry[0]);
                pstmt.setString(2, (String) entry[1]);
                pstmt.setInt(3, (int) entry[2]);
                pstmt.executeUpdate();
            }
        }
    }

    // Method to insert data into akun_3
    private static void insertAkunLevel3(Connection conn) throws SQLException {
        String sql = "INSERT INTO akun_3 (id, nama, definisi, id_akun2) VALUES (?, ?, ?, ?)";
        List<Object[]> data = List.of(
                new Object[]{111, "Kas", "Kas adalah alat pertukaran yang dapat diterima bank untuk disimpan seperti koin, uang kertas, cek, uang deposito, dan lain-lain.", 1},
                new Object[]{113, "Perlengkapan", "Perlengkapan adalah barang habis pakai yang berguna sebagai penunjang operasional perusahaan.", 1},
                new Object[]{122, "Peralatan", "Peralatan adalah berbagai barang ataupun berbagai tempat yang dimanfaatkan oleh perusahaan untuk menjalankan seluruh kegiatan bisnis perusahaan.", 1},
                new Object[]{114, "Persediaan Bahan Baku", "Akun ini untuk menampung nilai persediaan yang terdiri dari Persediaan Bahan Makanan & Minuman. Dan terperinci dalam persediaan per category di modul persediaan.", 1},
                new Object[]{115, "Persediaan Bahan Baku Dagang", "Persediaan barang dagang adalah barang-barang yang dibutuhkan untuk dijual kembali.", 1},
                new Object[]{103, "Piutang Usaha", "Piutang Usaha adalah aktiva yang meliputi seluruh uang yang dipinjamkan perusahaan karena penjualan kredit belum ditagih di masa lampau.", 1},
                new Object[]{108, "Sewa Dibayar Dimuka", "Sewa dibayar di muka adalah aktiva yang menunjukkan pembayaran sewa bangunan di muka.", 1},
                new Object[]{121, "Tanah", "Tanah adalah real estate yang ditahan untuk kegunaan produktif.", 2},
                new Object[]{124, "Bangunan", "Bangunan adalah aktiva tetap yang berbentuk gedung untuk usaha.", 2},
                new Object[]{123, "Akumulasi Penyusutan Peralatan", "Akumulasi penyusutan peralatan adalah jumlah penyusutan yang terkumpul dan mengurangi nilai aktiva tetap-peralatan.", 3},
                new Object[]{125, "Akumulasi Penyusutan Bangunan", "Akumulasi penyusutan bangunan adalah jumlah penyusutan yang terkumpul dan mengurangi nilai aktiva tetap-bangunan.", 3},
                new Object[]{211, "Utang Usaha", "Utang Usaha adalah utang yang menunjukkan jumlah yang dipinjam oleh perusahaan karena pembelian kredit belum dibayar di masa lampau.", 4},
                new Object[]{212, "Utang Bank", "Utang bank adalah pinjaman modal kerja dari bank untuk perluasan usaha.", 5},
                new Object[]{300, "Modal", "Modal adalah modal yang disetorkan oleh pemilik, baik berupa uang ataupun barang, yang digunakan untuk operasional perusahaan.", 6},
                new Object[]{312, "Prive", "Prive adalah pengambilan uang atau barang oleh pemilik untuk keperluan pribadi.", 6},
                new Object[]{400, "Penjualan Barang", "Penjualan adalah pendapatan yang diterima dari pertukaran barang atau jasa.", 7},
                new Object[]{401, "Retur Penjualan", "Retur penjualan adalah barang-barang tertentu yang dikembalikan kepada penjual karena cacat/rusak/tidak sesuai spesifikasi yang dipesan.", 7},
                new Object[]{402, "Potongan Penjualan", "Potongan penjualan adalah kas atau uang tunai yang diberikan oleh penjual kepada pembeli dalam periode awal pembayaran perkiraan.", 7},
                new Object[]{500, "Pembelian", "Merupakan kegiatan untuk pengadaan barang yang dibutuhkan perusahaan.", 8},
                new Object[]{501, "Beban Angkut Pembelian", "Merupakan suatu transaksi yang terjadi ketika perusahaan menggunakan jasa angkut.", 8},
                new Object[]{502, "Potongan Pembelian", "Merupakan pengurangan terhadap harga pokok persediaan.", 8},
                new Object[]{511, "Harga Pokok Penjualan", "Harga pokok penjualan adalah keseluruhan pengeluaran yang dikeluarkan untuk pengadaan barang dagang.", 8},
                new Object[]{600, "Biaya Pengiriman", "Biaya pengiriman adalah transaksi pembelian barang dagang seharusnya telah disepakati apakah penjual atau pembeli yang akan membayar atau menanggung semua ongkos angkut barang mulai dari gudang penjual hingga gudang pembeli.", 9},
                new Object[]{601, "Biaya Penjualan Lain-Lain", "Penjualan barang dagang diluar usaha.", 9},
                new Object[]{610, "Biaya Telepon, Listrik, dan Air", "Beban telepon, listrik, dan air adalah pembayaran telepon, listrik, dan air yang dibebankan ke perusahaan.", 10},
                new Object[]{612, "Biaya Gaji Karyawan", "Beban gaji adalah pembayaran jasa-jasa karyawan yang dibebankan ke perusahaan.", 10},
                new Object[]{615, "Biaya Perlengkapan", "Beban perlengkapan adalah pembayaran perlengkapan yang dibebankan ke perusahaan.", 10},
                new Object[]{616, "Biaya Tempat Sewa Usaha", "Beban sewa adalah pembayaran sewa bangunan yang dibebankan ke perusahaan.", 10},
                new Object[]{621, "Beban Piutang Tak Tertagih", "Beban piutang tak tertagih adalah kerugian piutang tak tertagih yang dibebankan ke perusahaan.", 10},
                new Object[]{800, "Pendapatan Bunga Bank", "Pendapatan bunga bank adalah pendapatan yang didapat oleh individu atau perusahaan, dikarenakan memiliki sejumlah dana yang tersimpan di bank.", 10}
        );

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] entry : data) {
                pstmt.setInt(1, (int) entry[0]); // Menggunakan kolom id sebagai kode_akun
                pstmt.setString(2, (String) entry[1]);
                pstmt.setString(3, (String) entry[2]);
                pstmt.setInt(4, (int) entry[3]); // Pastikan ini sesuai dengan tipe data kolom id_akun2
                pstmt.executeUpdate();
            }
        }
    }

}
