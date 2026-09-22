public class ContaCorrente extends Conta {

    public ContaCorrente(int numero, String titular, double saldo) {
        super(numero, titular, saldo);
    }

    @Override
    public void sacar(double valor) throws SaldoInsuficienteException {
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor de saque deve ser maior que zero.");
        }
        if (valor > saldo) {
            throw new SaldoInsuficienteException("Saldo insuficiente para saque de " + valor);
        }
        saldo -= valor;
    }
}
