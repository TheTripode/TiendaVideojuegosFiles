package es.dam.di.repository.file;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import es.dam.di.model.Account;
import es.dam.di.model.Email;
import es.dam.di.repository.IAccountRepository;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountRepositoryFile implements IAccountRepository {

    private static final String FILE_PATH = "data/accounts.json";
    private final Gson gson = GsonManager.getGson();

    @Override
    public <S extends Account> S save(S entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        List<Account> accounts = load();

        // insert
        if (entity.getId() == null) {
            // busca el ID más alto para saber que ID meter
            long newId = accounts.stream()
                    .mapToLong(Account::getId)
                    .max()
                    .orElse(0L) + 1;
            entity.setId(newId);
            accounts.add(entity);
            //update
        } else {
            int index = -1;
            for (int i = 0; i < accounts.size(); i++) {
                if (accounts.get(i).getId().equals(entity.getId())) {
                    index = i;
                    break;
                }
            }
            if (index != -1) { // si encontramos el index sustituimos
                accounts.set(index, entity);
            } else { // si por lo que sea no lo encontramos pues añadimos
                accounts.add(entity);
            }
        }

        saveToFile(accounts);
        return entity;
    }

    @Override
    public Account findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        return load().stream()
                .filter(a -> id.equals(a.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Optional<Account> findByIdOptional(Long id) {
        return Optional.ofNullable(findById(id));
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        return findById(id) != null;
    }

    @Override
    public List<Account> findAllToList() {
        return load();
    }

    @Override
    public Iterable<Account> findAll() {
        return findAllToList();
    }

    @Override
    public long count() {
        return load().size();
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        List<Account> accounts = load();
        boolean eliminado = accounts.removeIf(a -> id.equals(a.getId()));
        if (eliminado) {
            saveToFile(accounts);
        }
    }

    @Override
    public void deleteAll() {
        saveToFile(new ArrayList<>());
    }

    //Métodos propios

    @Override
    public Optional<Account> findByEmail(Email email) {
        if (email == null) {
            throw new IllegalArgumentException("El email no puede ser nulo");
        }
        return load().stream()
                .filter(a -> a.getEmail() != null && a.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<Account> findByUserName(String userName) {
        if (userName == null) {
            throw new IllegalArgumentException("El nombre de usuario no puede ser nulo");
        }
        return load().stream()
                .filter(a -> a.getUserName() != null && a.getUserName().equalsIgnoreCase(userName))
                .findFirst();
    }

    //Métodos repetitivos

    private List<Account> load() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        try (Reader reader = new FileReader(file)) {
            // TypeToken sirve para guardar el tipo de objeto que hay dentro de la List para que Gson lo pueda saber,
            // si no no tiene forma de saberlo
            Type listType = new TypeToken<List<Account>>() {}.getType();
            List<Account> accounts = gson.fromJson(reader, listType);
            return accounts != null ? accounts : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar las cuentas desde " + FILE_PATH, e);
        }
    }

    private void saveToFile(List<Account> accounts) {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(accounts, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar las cuentas en " + FILE_PATH, e);
        }
    }
}