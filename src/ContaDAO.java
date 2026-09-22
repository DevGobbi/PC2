import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContaDAO {

    public void inserir(ContaCorrente conta) {
        String sql = "INSERT INTO contas (numero, titular, saldo) VALUES (?, ?, ?)";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, conta.getNumero());
            stmt.setString(2, conta.getTitular());
            stmt.setDouble(3, conta.getSaldo());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao inserir conta: " + e.getMessage());
        }
    }

    public List<ContaCorrente> listar() {
        List<ContaCorrente> contas = new ArrayList<>();
        String sql = "SELECT numero, titular, saldo FROM contas";
        try (Connection conexao = Conexao.abrir();
             Statement stmt = conexao.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                contas.add(new ContaCorrente(
                        rs.getInt("numero"),
                        rs.getString("titular"),
                        rs.getDouble("saldo")));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar contas: " + e.getMessage());
        }
        return contas;
    }

    public ContaCorrente buscarPorNumero(int numero) {
        String sql = "SELECT numero, titular, saldo FROM contas WHERE numero = ?";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, numero);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ContaCorrente(
                            rs.getInt("numero"),
                            rs.getString("titular"),
                            rs.getDouble("saldo"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar conta: " + e.getMessage());
        }
        return null;
    }

    public void atualizarSaldo(int numero, double novoSaldo) {
        String sql = "UPDATE contas SET saldo = ? WHERE numero = ?";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setDouble(1, novoSaldo);
            stmt.setInt(2, numero);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar saldo: " + e.getMessage());
        }
    }

    public void transferir(int numeroOrigem, int numeroDestino, double valor, TarifaStrategy estrategia) throws SQLException {
        if (valor <= 0) {
            throw new SQLException("Valor da transferencia deve ser maior que zero.");
        }
        if (numeroOrigem == numeroDestino) {
            throw new SQLException("Conta de origem e destino nao podem ser a mesma.");
        }

        double tarifa = new TarifaService().calcularTarifa(estrategia, valor);
        double totalDebito = valor + tarifa;

        String sqlDebitar = "UPDATE contas SET saldo = saldo - ? WHERE numero = ? AND saldo >= ?";
        String sqlCreditar = "UPDATE contas SET saldo = saldo + ? WHERE numero = ?";

        Connection conexao = null;
        try {
            conexao = Conexao.abrir();
            conexao.setAutoCommit(false);

            try (PreparedStatement stmtDebito = conexao.prepareStatement(sqlDebitar)) {
                stmtDebito.setDouble(1, totalDebito);
                stmtDebito.setInt(2, numeroOrigem);
                stmtDebito.setDouble(3, totalDebito);
                int linhas = stmtDebito.executeUpdate();
                if (linhas == 0) {
                    throw new SQLException("Saldo insuficiente (valor + tarifa de R$ " + tarifa + ") ou conta de origem inexistente.");
                }
            }

            try (PreparedStatement stmtCredito = conexao.prepareStatement(sqlCreditar)) {
                stmtCredito.setDouble(1, valor);
                stmtCredito.setInt(2, numeroDestino);
                int linhas = stmtCredito.executeUpdate();
                if (linhas == 0) {
                    throw new SQLException("Conta de destino inexistente.");
                }
            }

            new TransferenciaDAO().inserir(conexao, new Transferencia(numeroOrigem, numeroDestino, valor, tarifa));

            conexao.commit();
        } catch (SQLException e) {
            if (conexao != null) {
                conexao.rollback();
            }
            throw e;
        } finally {
            if (conexao != null) {
                conexao.setAutoCommit(true);
                Conexao.fechar(conexao);
            }
        }
    }

    public void remover(int numero) {
        String sql = "DELETE FROM contas WHERE numero = ?";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, numero);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao remover conta: " + e.getMessage());
        }
    }
}
