package es.dam.di.repository.file;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import es.dam.di.model.Library;
import es.dam.di.repository.ILibraryRepository;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LibraryRepositoryFile implements ILibraryRepository {

    private static final String FILE_PATH = "data/libraries.json";
    private final Gson gson = GsonManager.getGson();

    @Override
    public <S extends Library> S save(S entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        List<Library> libraries = load();

        int index = -1;
        for (int i = 0; i < libraries.size(); i++) {
            Library l = libraries.get(i);
            if (entity.getAccountId().equals(l.getAccountId()) && entity.getVideogameId().equals(l.getVideogameId())) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            libraries.set(index, entity);
        } else {
            libraries.add(entity);
        }

        saveToFile(libraries);
        return entity;
    }


    @Override
    public Library findById(Long id) {
        throw new UnsupportedOperationException("No se puede por clave compuesta");
    }

    @Override
    public Optional<Library> findByIdOptional(Long id) {
        throw new UnsupportedOperationException("No se puede por clave compuesta");
    }

    @Override
    public boolean existsById(Long id) {
        throw new UnsupportedOperationException("No se puede por clave compuesta");
    }

    @Override
    public void deleteById(Long id) {
        throw new UnsupportedOperationException("No se puede por clave compuesta");
    }


    @Override
    public List<Library> findAllToList() {
        return load();
    }

    @Override
    public Iterable<Library> findAll() {
        return findAllToList();
    }

    @Override
    public long count() {
        return load().size();
    }

    @Override
    public void deleteAll() {
        saveToFile(new ArrayList<>());
    }

    //Métodos propios

    @Override
    public List<Library> findByAccountId(Long accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("El ID de la cuenta no puede ser nulo");
        }
        return load().stream()
                .filter(l -> accountId.equals(l.getAccountId()))
                .toList();
    }

    @Override
    public Optional<Library> findByAccountIdAndVideogameId(Long accountId, Long videogameId) {
        if (accountId == null || videogameId == null) {
            throw new IllegalArgumentException("Los ID de cuenta y videojuego no pueden ser nulos");
        }
        return load().stream()
                .filter(l -> accountId.equals(l.getAccountId()) && videogameId.equals(l.getVideogameId()))
                .findFirst();
    }

    //Métodos repetitivos

    private List<Library> load() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Library>>() {}.getType();
            List<Library> libraries = gson.fromJson(reader, listType);
            return libraries != null ? libraries : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la biblioteca desde " + FILE_PATH, e);
        }
    }

    private void saveToFile(List<Library> libraries) {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(libraries, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la biblioteca en " + FILE_PATH, e);
        }
    }
}