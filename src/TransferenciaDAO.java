import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransferenciaDAO {

    public void inserir(Connection conexao, Transferencia t) throws SQLException {
        String sql = "INSERT INTO transferencias (numero_origem, numero_destino, valor, tarifa, data_hora) "
                + "VALUES (?, ?, ?, ?, NOW())";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, t.getNumeroOrigem());
            stmt.setInt(2, t.getNumeroDestino());
            stmt.setDouble(3, t.getValor());
            stmt.setDouble(4, t.getTarifa());
            stmt.executeUpdate();
        }
    }

    public List<Transferencia> listarTodas() {
        List<Transferencia> lista = new ArrayList<>();
        String sql = "SELECT id, numero_origem, numero_destino, valor, tarifa, data_hora "
                + "FROM transferencias ORDER BY data_hora DESC";
        try (Connection conexao = Conexao.abrir();
             Statement stmt = conexao.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Transferencia(
                        rs.getInt("id"),
                        rs.getInt("numero_origem"),
                        rs.getInt("numero_destino"),
                        rs.getDouble("valor"),
                        rs.getDouble("tarifa"),
                        rs.getTimestamp("data_hora")));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar transferencias: " + e.getMessage());
        }
        return lista;
    }

    public List<Transferencia> listarPorConta(int numero) {
        List<Transferencia> lista = new ArrayList<>();
        String sql = "SELECT id, numero_origem, numero_destino, valor, tarifa, data_hora "
                + "FROM transferencias WHERE numero_origem = ? OR numero_destino = ? ORDER BY data_hora DESC";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, numero);
            stmt.setInt(2, numero);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Transferencia(
                            rs.getInt("id"),
                            rs.getInt("numero_origem"),
                            rs.getInt("numero_destino"),
                            rs.getDouble("valor"),
                            rs.getDouble("tarifa"),
                            rs.getTimestamp("data_hora")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar transferencias da conta: " + e.getMessage());
        }
        return lista;
    }
}
