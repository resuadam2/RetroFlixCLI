import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RetroFlixManager {
    private static final String PELICULAS_FILE = "peliculas.txt";
    private static final String CLIENTES_FILE = "clientes.ser";
    private static final String LOG_FILE = "log.txt";

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        inicializarPeliculas();

        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1" -> listarPeliculas();
                case "2" -> registrarCliente();
                case "3" -> mostrarClientes();
                case "4" -> buscarCliente();
                case "5" -> eliminarCliente();
                case "6" -> registrarAlquiler(true);
                case "7" -> registrarAlquiler(false);
                case "8" -> salir = true;
                default -> System.out.println("Opción no válida.");
            }
        }

        System.out.println("¡Hasta luego!");
    }

    private static void mostrarMenu() {
        System.out.println("""
                \n--- Menú RetroFlix ---
                1. Listar películas
                2. Registrar nuevo cliente
                3. Mostrar clientes
                4. Buscar cliente por número
                5. Eliminar cliente
                6. Registrar alquiler de película
                7. Registrar devolución de película
                8. Salir
                Elige una opción:
                """);
    }

    private static void inicializarPeliculas() {
        Path path = Paths.get(PELICULAS_FILE);
        if (!Files.exists(path)) {
            List<String> peliculas = List.of(
                    "Regreso al Futuro",
                    "Los Cazafantasmas",
                    "Capitán Fantástico",
                    "Indiana Jones y el Templo Maldito",
                    "Blade Runner",
                    "Alien, el octavo pasajero",
                    "El Club de los Cinco",
                    "Terminator",
                    "La Historia Interminable",
                    "El Club de la Lucha"
            );
            try {
                Files.write(path, peliculas);
                System.out.println("Archivo 'peliculas.txt' creado con contenido predeterminado.");
            } catch (IOException e) {
                System.out.println("Error al crear 'peliculas.txt': " + e.getMessage());
            }
        }
    }

    private static void listarPeliculas() {
        try {
            Files.lines(Paths.get(PELICULAS_FILE)).forEach(System.out::println);
        } catch (IOException e) {
            System.out.println("Error al leer el archivo de películas.");
        }
    }

    private static void registrarCliente() {
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();

        int numero = pedirNumeroCliente("Número de cliente: ");
        if (numero == -1) return;

        List<Cliente> clientes = leerClientes();
        boolean yaExiste = clientes.stream().anyMatch(c -> c.getNumero() == numero);
        if (yaExiste) {
            System.out.println("Ya existe un cliente con ese número.");
            return;
        }

        Cliente nuevo = new Cliente(nombre, email, numero);
        clientes.add(nuevo);
        escribirClientes(clientes);
        System.out.println("Cliente registrado con éxito.");
    }

    private static void mostrarClientes() {
        List<Cliente> clientes = leerClientes();
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes registrados.");
        } else {
            clientes.forEach(System.out::println);
        }
    }

    private static void buscarCliente() {
        int numero = pedirNumeroCliente("Introduce número de cliente: ");
        if (numero == -1) return;

        List<Cliente> clientes = leerClientes();
        clientes.stream()
                .filter(c -> c.getNumero() == numero)
                .findFirst()
                .ifPresentOrElse(
                        System.out::println,
                        () -> System.out.println("Cliente no encontrado.")
                );
    }

    private static void eliminarCliente() {
        int numero = pedirNumeroCliente("Introduce número de cliente a eliminar: ");
        if (numero == -1) return;

        List<Cliente> clientes = leerClientes();
        boolean eliminado = clientes.removeIf(c -> c.getNumero() == numero);
        if (eliminado) {
            escribirClientes(clientes);
            System.out.println("Cliente eliminado.");
        } else {
            System.out.println("Cliente no encontrado.");
        }
    }

    private static void registrarAlquiler(boolean esAlquiler) {
        int numero = pedirNumeroCliente("Número de cliente: ");
        if (numero == -1) return;

        List<Cliente> clientes = leerClientes();
        boolean clienteExiste = clientes.stream().anyMatch(c -> c.getNumero() == numero);
        if (!clienteExiste) {
            System.out.println("No existe un cliente con ese número.");
            return;
        }

        System.out.print("Título de la película: ");
        String titulo = scanner.nextLine();

        try {
            boolean existe = Files.lines(Paths.get(PELICULAS_FILE))
                    .anyMatch(p -> p.equalsIgnoreCase(titulo));
            if (!existe) {
                System.out.println("La película no está disponible.");
                return;
            }
        } catch (IOException e) {
            System.out.println("Error al verificar película.");
            return;
        }

        String accion = esAlquiler ? "alquiló" : "devolvió";
        String log = String.format("[%s] Cliente %d %s \"%s\"",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                numero, accion, titulo);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            bw.write(log);
            bw.newLine();
            System.out.println("Operación registrada.");
        } catch (IOException e) {
            System.out.println("Error al escribir en el log.");
        }
    }


    private static List<Cliente> leerClientes() {
        List<Cliente> clientes = new ArrayList<>();
        File archivo = new File(CLIENTES_FILE);
        if (!archivo.exists()) return clientes;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            while (true) {
                clientes.add((Cliente) ois.readObject());
            }
        } catch (EOFException ignored) {
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error al leer clientes: " + e.getMessage());
        }

        return clientes;
    }

    private static void escribirClientes(List<Cliente> clientes) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CLIENTES_FILE))) {
            for (Cliente c : clientes) {
                oos.writeObject(c);
            }
        } catch (IOException e) {
            System.out.println("Error al guardar clientes.");
        }
    }

    private static int pedirNumeroCliente(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Número no válido. Inténtalo de nuevo.");
            }
        }
    }
}
