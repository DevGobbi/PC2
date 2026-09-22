import java.sql.Timestamp;

public class Transferencia {
    private int id;
    private int numeroOrigem;
    private int numeroDestino;
    private double valor;
    private double tarifa;
    private Timestamp dataHora;

    public Transferencia(int numeroOrigem, int numeroDestino, double valor, double tarifa) {
        this.numeroOrigem = numeroOrigem;
        this.numeroDestino = numeroDestino;
        this.valor = valor;
        this.tarifa = tarifa;
    }

    public Transferencia(int id, int numeroOrigem, int numeroDestino, double valor, double tarifa, Timestamp dataHora) {
        this.id = id;
        this.numeroOrigem = numeroOrigem;
        this.numeroDestino = numeroDestino;
        this.valor = valor;
        this.tarifa = tarifa;
        this.dataHora = dataHora;
    }

    public int getId() {
        return id;
    }

    public int getNumeroOrigem() {
        return numeroOrigem;
    }

    public int getNumeroDestino() {
        return numeroDestino;
    }

    public double getValor() {
        return valor;
    }

    public double getTarifa() {
        return tarifa;
    }

    public Timestamp getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        return dataHora + " | " + numeroOrigem + " -> " + numeroDestino
                + " | valor: R$ " + valor + " | tarifa: R$ " + tarifa;
    }
}
