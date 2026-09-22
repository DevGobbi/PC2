import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GerenciadorContas extends JFrame {
    private List<ContaCorrente> contas = new ArrayList<>();
    private DefaultListModel<ContaCorrente> modelo = new DefaultListModel<>();
    private JList<ContaCorrente> lista = new JList<>(modelo);
    private JLabel labelDetalhes = new JLabel("Selecione uma conta");
    private JTextField campoValor = new JTextField(10);
    private ContaDAO dao = new ContaDAO();

    public GerenciadorContas() {
        super("Gerenciador de Contas Bancárias");
        carregarContasDoBanco();

        setLayout(new BorderLayout(10, 10));
        add(new JScrollPane(lista), BorderLayout.WEST);

        JPanel painelDireita = new JPanel();
        painelDireita.setLayout(new BoxLayout(painelDireita, BoxLayout.Y_AXIS));
        painelDireita.add(labelDetalhes);

        JPanel painelOperacoes = new JPanel();
        JButton btnSacar = new JButton("Sacar");
        JButton btnDepositar = new JButton("Depositar");
        painelOperacoes.add(new JLabel("Valor:"));
        painelOperacoes.add(campoValor);
        painelOperacoes.add(btnSacar);
        painelOperacoes.add(btnDepositar);
        painelDireita.add(painelOperacoes);

        add(painelDireita, BorderLayout.CENTER);

        lista.addListSelectionListener(e -> atualizarDetalhes());
        btnSacar.addActionListener(e -> sacar());
        btnDepositar.addActionListener(e -> depositar());

        JButton btnAdicionar = new JButton("Adicionar Conta");
        JButton btnRemover = new JButton("Remover Conta");
        JButton btnFiltrar = new JButton("Saldo > 10000");
        JButton btnTotal = new JButton("Saldo Total");
        JButton btnAgrupar = new JButton("Agrupar por Faixa");
        JButton btnSalvar = new JButton("Salvar");

        btnAdicionar.addActionListener(e -> adicionarConta());
        btnRemover.addActionListener(e -> removerConta());
        btnFiltrar.addActionListener(e -> filtrarSaldoAlto());
        btnTotal.addActionListener(e -> saldoTotal());
        btnAgrupar.addActionListener(e -> agruparPorFaixa());
        btnSalvar.addActionListener(e -> salvarContas("contas_atualizadas.txt"));

        JPanel painelSul = new JPanel();
        painelSul.add(btnAdicionar);
        painelSul.add(btnRemover);
        painelSul.add(btnFiltrar);
        painelSul.add(btnTotal);
        painelSul.add(btnAgrupar);
        painelSul.add(btnSalvar);
        add(painelSul, BorderLayout.SOUTH);

        JButton btnOrdenarSaldo = new JButton("Ordenar por Saldo");
        JButton btnOrdenarTitular = new JButton("Ordenar por Titular");
        JButton btnFiltroSaldo5k = new JButton("Saldo > 5000");
        JButton btnFiltroNumeroPar = new JButton("Numero Par");
        JButton btnTarifa = new JButton("Calcular Tarifa");
        JButton btnTransferir = new JButton("Transferir");
        JButton btnHistorico = new JButton("Ver Historico");

        btnOrdenarSaldo.addActionListener(e -> ordenarPorSaldo());
        btnOrdenarTitular.addActionListener(e -> ordenarPorTitular());
        btnFiltroSaldo5k.addActionListener(e -> filtrarComPredicate(c -> c.getSaldo() > 5000, "Saldo > R$ 5000"));
        btnFiltroNumeroPar.addActionListener(e -> filtrarComPredicate(c -> c.getNumero() % 2 == 0, "Numero par"));
        btnTarifa.addActionListener(e -> calcularTarifa());
        btnTransferir.addActionListener(e -> transferir());
        btnHistorico.addActionListener(e -> verHistorico());

        JPanel painelSul2 = new JPanel();
        painelSul2.add(btnOrdenarSaldo);
        painelSul2.add(btnOrdenarTitular);
        painelSul2.add(btnFiltroSaldo5k);
        painelSul2.add(btnFiltroNumeroPar);
        painelSul2.add(btnTarifa);
        painelSul2.add(btnTransferir);
        painelSul2.add(btnHistorico);
        add(painelSul2, BorderLayout.NORTH);

        setSize(650, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void carregarContasDoBanco() {
        contas = dao.listar();
        contas.forEach(modelo::addElement);
    }

    private void salvarContas(String arquivo) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(arquivo))) {
            for (ContaCorrente c : contas) {
                bw.write(c.getNumero() + "," + c.getTitular() + "," + c.getSaldo());
                bw.newLine();
            }
            JOptionPane.showMessageDialog(this, "Contas salvas em " + arquivo);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + e.getMessage());
        }
    }

    private ContaCorrente getSelecionada() {
        ContaCorrente c = lista.getSelectedValue();
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma conta primeiro.");
        }
        return c;
    }

    private double lerValor() {
        try {
            double valor = Double.parseDouble(campoValor.getText().trim());
            if (valor <= 0) {
                JOptionPane.showMessageDialog(this, "O valor deve ser maior que zero.");
                return -1;
            }
            return valor;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valor invalido.");
            return -1;
        }
    }

    private void sacar() {
        ContaCorrente c = getSelecionada();
        if (c == null) return;
        double valor = lerValor();
        if (valor < 0) return;
        try {
            c.sacar(valor);
            dao.atualizarSaldo(c.getNumero(), c.getSaldo());
            JOptionPane.showMessageDialog(this, "Saque realizado com sucesso.");
        } catch (SaldoInsuficienteException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
        atualizarDetalhes();
        lista.repaint();
    }

    private void depositar() {
        ContaCorrente c = getSelecionada();
        if (c == null) return;
        double valor = lerValor();
        if (valor < 0) return;
        try {
            c.depositar(valor);
            dao.atualizarSaldo(c.getNumero(), c.getSaldo());
            JOptionPane.showMessageDialog(this, "Deposito realizado com sucesso.");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
        atualizarDetalhes();
        lista.repaint();
    }

    private void atualizarDetalhes() {
        ContaCorrente c = lista.getSelectedValue();
        if (c != null) {
            labelDetalhes.setText("<html>Numero: " + c.getNumero()
                    + "<br>Titular: " + c.getTitular()
                    + "<br>Saldo: R$ " + c.getSaldo() + "</html>");
        }
    }

    private void adicionarConta() {
        try {
            String numeroStr = JOptionPane.showInputDialog(this, "Numero da conta:");
            if (numeroStr == null) return;
            String titular = JOptionPane.showInputDialog(this, "Titular:");
            if (titular == null) return;
            String saldoStr = JOptionPane.showInputDialog(this, "Saldo inicial:");
            if (saldoStr == null) return;

            int numero = Integer.parseInt(numeroStr.trim());
            double saldo = Double.parseDouble(saldoStr.trim());

            if (numero <= 0) {
                JOptionPane.showMessageDialog(this, "Numero da conta deve ser maior que zero.");
                return;
            }
            if (titular.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Titular nao pode ser vazio.");
                return;
            }
            if (saldo < 0) {
                JOptionPane.showMessageDialog(this, "Saldo inicial nao pode ser negativo.");
                return;
            }
            if (contas.stream().anyMatch(c -> c.getNumero() == numero)) {
                JOptionPane.showMessageDialog(this, "Ja existe uma conta com esse numero.");
                return;
            }

            ContaCorrente c = new ContaCorrente(numero, titular.trim(), saldo);
            dao.inserir(c);
            contas.add(c);
            modelo.addElement(c);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Numero ou saldo invalido.");
        }
    }

    private void removerConta() {
        ContaCorrente c = getSelecionada();
        if (c == null) return;
        dao.remover(c.getNumero());
        contas.remove(c);
        refreshModelo();
    }

    private void filtrarSaldoAlto() {
        List<ContaCorrente> filtradas = contas.stream()
                .filter(c -> c.getSaldo() > 10000)
                .collect(Collectors.toList());

        if (filtradas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma conta com saldo > R$ 10000.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        filtradas.forEach(c -> sb.append(c).append("\n"));
        JOptionPane.showMessageDialog(this, sb.toString());
    }

    private void saldoTotal() {
        double total = contas.stream()
                .mapToDouble(ContaCorrente::getSaldo)
                .reduce(0, Double::sum);
        JOptionPane.showMessageDialog(this, "Saldo total: R$ " + total);
    }

    private void agruparPorFaixa() {
        Map<String, List<ContaCorrente>> porFaixa = contas.stream()
                .collect(Collectors.groupingBy(c -> {
                    if (c.getSaldo() <= 5000) return "Ate R$ 5000";
                    else if (c.getSaldo() <= 10000) return "De R$ 5001 a R$ 10000";
                    else return "Acima de R$ 10000";
                }));

        StringBuilder sb = new StringBuilder();
        porFaixa.forEach((faixa, lista) -> sb.append(faixa).append(": ").append(lista.size()).append(" conta(s)\n"));
        JOptionPane.showMessageDialog(this, sb.toString());
    }

    private void refreshModelo() {
        modelo.clear();
        contas.forEach(modelo::addElement);
    }

    private void ordenarPorSaldo() {
        contas.sort(Comparator.comparingDouble(ContaCorrente::getSaldo).reversed());
        refreshModelo();
    }

    private void ordenarPorTitular() {
        contas.sort(Comparator.comparing(ContaCorrente::getTitular));
        refreshModelo();
    }

    private void filtrarComPredicate(Predicate<ContaCorrente> filtro, String descricao) {
        List<ContaCorrente> resultado = contas.stream()
                .filter(filtro)
                .collect(Collectors.toList());

        if (resultado.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma conta atende: " + descricao);
            return;
        }
        StringBuilder sb = new StringBuilder(descricao + ":\n");
        resultado.forEach(c -> sb.append(c).append("\n"));
        JOptionPane.showMessageDialog(this, sb.toString());
    }

    private void calcularTarifa() {
        ContaCorrente c = getSelecionada();
        if (c == null) return;

        String[] opcoes = {"FIXA", "PERCENTUAL", "ISENTA"};
        String escolha = (String) JOptionPane.showInputDialog(this, "Escolha a estrategia:",
                "Tarifa", JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
        if (escolha == null) return;

        TarifaStrategy estrategia = TarifaStrategy.valueOf(escolha);
        double tarifa = new TarifaService().calcularTarifa(estrategia, c.getSaldo());
        JOptionPane.showMessageDialog(this, "Tarifa (" + escolha + "): R$ " + tarifa);
    }

    private void transferir() {
        ContaCorrente origem = getSelecionada();
        if (origem == null) return;

        String destinoStr = JOptionPane.showInputDialog(this, "Numero da conta de destino:");
        if (destinoStr == null) return;
        double valor = lerValor();
        if (valor < 0) return;

        String[] opcoes = {"FIXA", "PERCENTUAL", "ISENTA"};
        String escolha = (String) JOptionPane.showInputDialog(this, "Estrategia de tarifa:",
                "Transferencia", JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
        if (escolha == null) return;

        try {
            int destino = Integer.parseInt(destinoStr.trim());
            if (destino == origem.getNumero()) {
                JOptionPane.showMessageDialog(this, "Conta de origem e destino nao podem ser a mesma.");
                return;
            }
            TarifaStrategy estrategia = TarifaStrategy.valueOf(escolha);
            dao.transferir(origem.getNumero(), destino, valor, estrategia);
            contas = dao.listar();
            refreshModelo();
            JOptionPane.showMessageDialog(this, "Transferencia realizada com sucesso.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Numero de conta invalido.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro na transferencia: " + e.getMessage());
        }
    }

    private void verHistorico() {
        List<Transferencia> historico = new TransferenciaDAO().listarTodas();
        if (historico.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma transferencia registrada.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        historico.forEach(t -> sb.append(t).append("\n"));

        JTextArea area = new JTextArea(sb.toString(), 15, 40);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Historico de Transferencias", JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GerenciadorContas().setVisible(true));
    }
}
