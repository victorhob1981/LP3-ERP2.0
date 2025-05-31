package UTIL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBanco {

    // URL de conexão com o banco
    private static final String URL = "jdbc:mysql://localhost:3306/erp_simples?useSSL=false";
    private static final String USUARIO = "root";    // Usuário do banco
    private static final String SENHA = "victorhugo1"; // Senha do banco

    // Método que retorna a conexão
    public static Connection conectar() throws SQLException {
        // Tenta estabelecer a conexão
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    public static void main(String[] args) {
        try (Connection conexao = conectar()) {
            if (conexao != null) {
                System.out.println("Conexão com o banco de dados estabelecida com sucesso!");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao conectar no banco: " + e.getMessage());
        }
    }
}
