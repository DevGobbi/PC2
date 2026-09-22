CREATE DATABASE IF NOT EXISTS banco_digital;
USE banco_digital;

CREATE TABLE IF NOT EXISTS contas (
    numero INT PRIMARY KEY,
    titular VARCHAR(100) NOT NULL,
    saldo DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS transferencias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero_origem INT NOT NULL,
    numero_destino INT NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    tarifa DECIMAL(10,2) NOT NULL,
    data_hora DATETIME NOT NULL,
    FOREIGN KEY (numero_origem) REFERENCES contas(numero) ON DELETE CASCADE,
    FOREIGN KEY (numero_destino) REFERENCES contas(numero) ON DELETE CASCADE
);
