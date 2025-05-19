import java.io.Serializable;

public class Cliente implements Serializable {
    private String nombre;
    private String email;
    private int numero;

    public Cliente(String nombre, String email, int numero) {
        this.nombre = nombre;
        this.email = email;
        this.numero = numero;
    }

    public int getNumero() {
        return numero;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", numero=" + numero +
                '}';
    }
}
