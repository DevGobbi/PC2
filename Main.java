public class Main {
    public static void main(String[] args) throws SaldoInsuficienteException, java.sql.SQLException {
        ContaDAO dao = new ContaDAO();

        System.out.println("--- Inserindo contas ---");
        ContaCorrente origem = new ContaCorrente(998, "Conta Origem", 1000.0);
        ContaCorrente destino = new ContaCorrente(999, "Conta Destino", 100.0);
        dao.inserir(origem);
        dao.inserir(destino);

        System.out.println("--- Listando contas ---");
        for (ContaCorrente c : dao.listar()) {
            System.out.println(c);
        }

        System.out.println("--- Testando deposito ---");
        origem.depositar(200.0);
        dao.atualizarSaldo(origem.getNumero(), origem.getSaldo());
        System.out.println("Saldo apos deposito: " + dao.buscarPorNumero(998));

        System.out.println("--- Testando saque ---");
        origem.sacar(500.0);
        dao.atualizarSaldo(origem.getNumero(), origem.getSaldo());
        System.out.println("Saldo apos saque: " + dao.buscarPorNumero(998));

        System.out.println("--- Testando transferencia com tarifa PERCENTUAL ---");
        dao.transferir(998, 999, 100.0, TarifaStrategy.PERCENTUAL);
        System.out.println("Origem apos transferencia: " + dao.buscarPorNumero(998));
        System.out.println("Destino apos transferencia: " + dao.buscarPorNumero(999));

        System.out.println("--- Testando transferencia com saldo insuficiente (deve falhar) ---");
        try {
            dao.transferir(998, 999, 999999.0, TarifaStrategy.FIXA);
        } catch (java.sql.SQLException e) {
            System.out.println("Erro esperado: " + e.getMessage());
        }

        System.out.println("--- Consultando historico ---");
        for (Transferencia t : new TransferenciaDAO().listarTodas()) {
            System.out.println(t);
        }

        System.out.println("--- Testando exclusao ---");
        dao.remover(999);
        dao.remover(998);
        System.out.println("Conta apos remocao: " + dao.buscarPorNumero(999));
    }
}

