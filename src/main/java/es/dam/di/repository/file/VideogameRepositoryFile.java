package es.dam.di.repository.file;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import es.dam.di.model.Videogame;
import es.dam.di.repository.IVideogameRepository;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VideogameRepositoryFile implements IVideogameRepository {

    private static final String FILE_PATH = "data/videogames.json";
    private final Gson gson = GsonManager.getGson();

    @Override
    public <S extends Videogame> S save(S entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        List<Videogame> videogames = load();

        if (entity.getId() == null) {
            long newId = videogames.stream()
                    .mapToLong(Videogame::getId)
                    .max()
                    .orElse(0L) + 1;
            entity.setId(newId);
            videogames.add(entity);
        } else {
            int index = -1;
            for (int i = 0; i < videogames.size(); i++) {
                if (videogames.get(i).getId().equals(entity.getId())) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                videogames.set(index, entity);
            } else {
                videogames.add(entity);
            }
        }

        saveToFile(videogames);
        return entity;
    }

    @Override
    public Videogame findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        return load().stream()
                .filter(v -> id.equals(v.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Optional<Videogame> findByIdOptional(Long id) {
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
    public List<Videogame> findAllToList() {
        return load();
    }

    @Override
    public Iterable<Videogame> findAll() {
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
        List<Videogame> videogames = load();
        boolean eliminado = videogames.removeIf(v -> id.equals(v.getId()));
        if (eliminado) {
            saveToFile(videogames);
        }
    }

    @Override
    public void deleteAll() {
        saveToFile(new ArrayList<>());
    }

    //Métodos propios

    @Override
    public List<Videogame> findByDeveloperId(Long developerId) {
        if (developerId == null) {
            throw new IllegalArgumentException("El ID del desarrollador no puede ser nulo");
        }
        return load().stream()
                .filter(v -> developerId.equals(v.getDeveloperId()))
                .toList();
    }

    @Override
    public List<Videogame> findByCategoryId(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("El ID de la categoría no puede ser nulo");
        }
        return load().stream()
                .filter(v -> v.getCategoryIds() != null && v.getCategoryIds().contains(categoryId))
                .toList();
    }

    //Métodos repetitivos

    private List<Videogame> load() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Videogame>>() {}.getType();
            List<Videogame> videogames = gson.fromJson(reader, listType);
            return videogames != null ? videogames : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar los videojuegos desde " + FILE_PATH, e);
        }
    }

    private void saveToFile(List<Videogame> videogames) {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(videogames, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar los videojuegos en " + FILE_PATH, e);
        }
    }
}