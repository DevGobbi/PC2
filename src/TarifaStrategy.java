public enum TarifaStrategy {
    FIXA {
        public double calcular(double saldo) {
            return 10.0;
        }
    },
    PERCENTUAL {
        public double calcular(double saldo) {
            return saldo * 0.01;
        }
    },
    ISENTA {
        public double calcular(double saldo) {
            return 0.0;
        }
    };

    public abstract double calcular(double saldo);
}
