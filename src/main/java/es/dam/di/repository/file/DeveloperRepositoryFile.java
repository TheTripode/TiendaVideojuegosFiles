package es.dam.di.repository.file;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import es.dam.di.model.Developer;
import es.dam.di.repository.IDeveloperRepository;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeveloperRepositoryFile implements IDeveloperRepository {

    private static final String FILE_PATH = "data/developers.json";
    private final Gson gson = GsonManager.getGson();

    @Override
    public <S extends Developer> S save(S entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        List<Developer> developers = load();

        if (entity.getId() == null) {
            long newId = developers.stream()
                    .mapToLong(Developer::getId)
                    .max()
                    .orElse(0L) + 1;
            entity.setId(newId);
            developers.add(entity);
        } else {
            int index = -1;
            for (int i = 0; i < developers.size(); i++) {
                if (developers.get(i).getId().equals(entity.getId())) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                developers.set(index, entity);
            } else {
                developers.add(entity);
            }
        }

        saveToFile(developers);
        return entity;
    }

    @Override
    public Developer findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        return load().stream()
                .filter(d -> id.equals(d.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Optional<Developer> findByIdOptional(Long id) {
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
    public List<Developer> findAllToList() {
        return load();
    }

    @Override
    public Iterable<Developer> findAll() {
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
        List<Developer> developers = load();
        boolean eliminado = developers.removeIf(d -> id.equals(d.getId()));
        if (eliminado) {
            saveToFile(developers);
        }
    }

    @Override
    public void deleteAll() {
        saveToFile(new ArrayList<>());
    }

    //Métodos propios

    @Override
    public Optional<Developer> findByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("El nombre no puede ser nulo");
        }
        return load().stream()
                .filter(d -> d.getName() != null && d.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Developer> findByNameContaining(String name) {
        if (name == null) {
            throw new IllegalArgumentException("El trozo de nombre no puede ser nulo");
        }
        return load().stream()
                .filter(d -> d.getName() != null && d.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    //Métodos repetitivos

    private List<Developer> load() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Developer>>() {}.getType();
            List<Developer> developers = gson.fromJson(reader, listType);
            return developers != null ? developers : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar los desarrolladores desde " + FILE_PATH, e);
        }
    }

    private void saveToFile(List<Developer> developers) {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(developers, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar los desarrolladores en " + FILE_PATH, e);
        }
    }
}