import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Principal {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Persistencia persistencia = new Persistencia();
                DadosUsuario dados = persistencia.carregarDados();

                if (persistencia.isDadosPadraoUsados()) {
                    String nome = JOptionPane.showInputDialog(null, "Digite seu nome ou apelido:");
                    if (nome != null && !nome.trim().isEmpty()) dados.getUsuario().setNome(nome);
                    if (!persistencia.salvarDados(dados)) {
                        JOptionPane.showMessageDialog(null, "Nao foi possivel salvar os dados.");
                    }
                }

                if (persistencia.getAvisoCarregamento() != null) {
                    JOptionPane.showMessageDialog(null, persistencia.getAvisoCarregamento());
                }

                TelaPrincipal tela = new TelaPrincipal(dados, persistencia);
                tela.setVisible(true);
            }
        });
    }
}
