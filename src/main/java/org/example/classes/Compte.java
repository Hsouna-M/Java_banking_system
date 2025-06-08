package org.example.classes;
import org.example.database.ConnectionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;import java.util.Date;

public abstract class Compte {
    private String numero;
    private String type;
    private double solde;
    private boolean estBlockee;
    private Date dateOuverture;

    public Compte(String numero, String type) {
        this(numero, type, 0.0, false); // default solde = 0.0, estBlockee = false
    }

    public Compte(String numero, String type, double solde, boolean estBlockee) {
        this.numero = numero;
        this.type = type;
        this.solde = solde;
        this.estBlockee = estBlockee;
        this.dateOuverture = new Date();
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public double getSolde() {
        return solde;
    }

    public void setSolde(double solde) {
        this.solde = solde;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type=type;
    }

    public abstract boolean retirer(double montant);
    public abstract boolean deposer(double montant);

    @Override
    public String toString() {
        return "Compte{" +
                "RIB=" + numero +
                ", type='" + type + '\'' +
                ", solde=" + solde +
                ", estBlockee=" + estBlockee +
                ", dateOuverture=" + dateOuverture +
                '}';
    }

    protected Date getDateOuverture() {
        return dateOuverture;
    }
    public boolean isEstBlockee() {
        return estBlockee;
    }

    public void setEstBlockee(boolean estBlockee) {
        this.estBlockee = estBlockee;
    }

    /**
     * Retrieves a Compte object from the database by its account number.
     *
     * @param numero The account number.
     * @return A CompteCourant or CompteEpargne object, or null if not found.
     */
    public static Compte getCompteByNumero(String numero) {
        String sql = "SELECT * FROM compte WHERE numero = ?";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, numero);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String type = rs.getString("type_compte");
                boolean estBlockee = rs.getBoolean("estBlockee");

                Compte compte;
                if ("Courant".equalsIgnoreCase(type)) {
                    compte = new CompteCourant(numero);
                } else if ("Epargne".equalsIgnoreCase(type)) {
                    compte = new CompteEpargne(numero);
                } else {
                    System.err.println("Unsupported account type: " + type);
                    return null;
                }

                compte.setEstBlockee(estBlockee);
                return compte;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}