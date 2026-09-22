public class TarifaService {
    public double calcularTarifa(TarifaStrategy estrategia, double saldo) {
        return estrategia.calcular(saldo);
    }
}
