import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Principal {
	static Scanner lector;

	public static void main(String[] args) throws ClassNotFoundException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		lector = new Scanner(System.in);
		Agenda agenda = null;

		File fichero = new File("agenda.dat");
		File ficheroclave = new File("Clave.dat");
		Cipher descifrador = Cipher.getInstance("DES");
		SecretKey clave;
		
		int opc = 0;

		if (fichero.exists()) {
			leerFichero(agenda);
		}else 
			agenda = new Agenda();
		if (ficheroclave.exists()){
				clave = leerClave();
				Descifrando(clave, agenda, descifrador);
				agenda = Descifrando(clave, agenda, descifrador);
			}else {
			// Si no existe fichero agenda y tampoco hay clave.
			// Creamos el generador de claves
				KeyGenerator generador = KeyGenerator.getInstance("DES");
			// Generar una clave
				clave = generador.generateKey();
				descifrador.init(Cipher.ENCRYPT_MODE, clave);
			}
		while (opc != 5) {
			mostrarMenu();
			opc = lector.nextInt();
			lector.nextLine(); // Para recoger el retorno de carro.
			switch (opc) {
			case 1:
				nuevoContacto(agenda);
				break;
			case 2:
				borrarContacto(agenda);
				break;
			case 3:
				consultarContacto(agenda);
				break;
			case 4:
				listadoContactos(agenda);
				break;
			}
		}
		// Ciframos y generamos el archivo con la agenda que devuelve el metodo
		// "Cifrando"
		Cifrando(descifrador, clave, agenda);
		
		crearClave(clave);		
		crearFichero(Cifrando(descifrador, clave, agenda));
		lector.close();
	}

	public static void mostrarMenu() {
		System.out.println(" AGENDA TELEFÓNICA");
		System.out.println("---------------------------------------");
		System.out.println("1. Añadir nuevo contacto");
		System.out.println("2. Borrar contacto");
		System.out.println("3. Consultar contacto");
		System.out.println("4. Listado de contactos");
		System.out.println("5. Terminar programa");
		System.out.println("---------------------------------------");
		System.out.println("¿Qué opción eliges?");
	}

	public static void nuevoContacto(Agenda agenda) {
		System.out.println("Nombre: ");
		String nombre = lector.nextLine();
		System.out.println("Teléfono: ");
		String tlf = lector.nextLine();
		agenda.getContactos().add(new Contacto(nombre, tlf));
		System.out.println("El contacto ha sido añadido con éxito");
	}

	public static void borrarContacto(Agenda agenda) {
		int i = 0;
		System.out.println("Nombre buscado: ");
		String nombre = lector.nextLine();
		while (i < agenda.getContactos().size() && !agenda.getContactos().get(i).getNombre().equals(nombre)) {
			i++;
		}
		if (i == agenda.getContactos().size()) {
			System.out.println("No encontrado");
		} else {
			System.out.println(agenda.getContactos().get(i) + " SERÁ ELIMINADO");
			agenda.getContactos().remove(i);
		}
	}

	public static void consultarContacto(Agenda agenda) {
		int i = 0;
		System.out.println("Nombre buscado: ");
		String nombre = lector.nextLine();
		while (i < agenda.getContactos().size() && !agenda.getContactos().get(i).getNombre().equals(nombre)) {
			i++;
		}
		if (i == agenda.getContactos().size()) {
			System.out.println("No encontrado");
		} else {
			System.out.println("Teléfono de " + nombre + ": " + agenda.getContactos().get(i).getTelefono());
		}
	}

	public static void listadoContactos(Agenda agenda) {
		for (Contacto c : agenda.getContactos()) {
			System.out.println(c);
		}
	}

	public static void crearFichero(Agenda agenda) throws IOException {
		FileOutputStream file = new FileOutputStream("agenda.dat");
		ObjectOutputStream buffer = new ObjectOutputStream(file);
		buffer.writeObject(agenda);
		buffer.close();
		file.close();
	}

	public static void leerFichero(Agenda agenda) throws IOException, ClassNotFoundException {
		FileInputStream file = new FileInputStream("agenda.dat");
		ObjectInputStream buffer = new ObjectInputStream(file);
		agenda = (Agenda) buffer.readObject();
		buffer.close();
		file.close();
	}
	
	public static void crearClave(SecretKey clave) throws IOException {
		byte[] claveBytes = clave.getEncoded();
		FileOutputStream file = new FileOutputStream("Clave.dat");
		ObjectOutputStream buffer = new ObjectOutputStream(file);
		buffer.writeObject(claveBytes);
		buffer.close();
		file.close();
	}

	public static SecretKey leerClave() throws IOException, ClassNotFoundException {

		FileInputStream file = new FileInputStream("Clave.dat");
		byte[] claveEncriptada = new byte[file.available()];
		file.read();
		file.close();
		SecretKey claveExistente = new SecretKeySpec(claveEncriptada, "DES");

		return claveExistente;
	}
	
	public static Agenda Cifrando(Cipher cifrar, SecretKey clave, Agenda agenda) throws UnsupportedEncodingException {

		// Creamos el generador de claves:
		try {

			// Recorro el contenido de la agenda, y voy cifrando
			for (Contacto c : agenda.getContactos()) {

				// Ciframos el contenido de la agenda:
				byte[] bytesNombreOriginal = c.getNombre().getBytes();
				byte[] bytesTelefonoOriginal = c.getTelefono().getBytes();
				byte[] bytesNombreCifrado = Base64.getEncoder().encode(bytesNombreOriginal);
				byte[] bytesTelefonoCifrado = Base64.getEncoder().encode(bytesTelefonoOriginal);
				String NombresCifrados = new String(bytesNombreCifrado);
				String TelefonosCifrados = new String(bytesTelefonoCifrado);
				System.out.println(c);
				System.out.println(c.getNombre()+ " = "+ NombresCifrados);
				System.out.println(c.getTelefono()+ " = "+TelefonosCifrados);
				c.setNombre(NombresCifrados);
				c.setTelefono(TelefonosCifrados);
			}
		} finally {
		}
		// Devolvemos la agenda con los nombres y telefonos cifrados.
		return agenda;

	}

	public static Agenda Descifrando(SecretKey claveExistente, Agenda agenda, Cipher descifrador)
			throws InvalidKeyException {

		try {
			for (Contacto c : agenda.getContactos()) {
				
				byte[] bytesNombreCifrado = c.getNombre().getBytes();
				byte[] bytesTelefonoCifrado = c.getTelefono().getBytes();
				byte[] bytesNombreDescifrado = Base64.getDecoder().decode(bytesNombreCifrado);
				byte[] bytesTelefonoDescifrado = Base64.getDecoder().decode(bytesTelefonoCifrado);
				String NombresDescifrados = new String(bytesNombreDescifrado);
				String TelefonosDescifrados = new String(bytesTelefonoDescifrado);
				System.out.println("Nombre: " + NombresDescifrados);
				System.out.println("Tlfn: " + TelefonosDescifrados);
				c.setNombre(NombresDescifrados);
				c.setTelefono(TelefonosDescifrados);
			}
		} finally {
		}
		return agenda;
	}

	
}
