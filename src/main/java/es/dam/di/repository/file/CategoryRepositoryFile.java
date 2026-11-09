package es.dam.di.repository.file;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import es.dam.di.model.Category;
import es.dam.di.model.Videogame;
import es.dam.di.repository.ICategoryRepository;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryRepositoryFile implements ICategoryRepository {

    private static final String FILE_PATH = "data/categories.json";
    private static final String VIDEOGAMES_FILE_PATH = "data/videogames.json";
    private final Gson gson = GsonManager.getGson();

    @Override
    public <S extends Category> S save(S entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        List<Category> categories = load();

        if (entity.getId() == null) {
            long newId = categories.stream()
                    .mapToLong(Category::getId)
                    .max()
                    .orElse(0L) + 1;
            entity.setId(newId);
            categories.add(entity);
        } else {
            int index = -1;
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId().equals(entity.getId())) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                categories.set(index, entity);
            } else {
                categories.add(entity);
            }
        }

        saveToFile(categories);
        return entity;
    }

    @Override
    public Category findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        return load().stream()
                .filter(c -> id.equals(c.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Optional<Category> findByIdOptional(Long id) {
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
    public List<Category> findAllToList() {
        return load();
    }

    @Override
    public Iterable<Category> findAll() {
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
        List<Category> categories = load();
        boolean eliminado = categories.removeIf(c -> id.equals(c.getId()));
        if (eliminado) {
            saveToFile(categories);
        }
    }

    @Override
    public void deleteAll() {
        saveToFile(new ArrayList<>());
    }

    //Métodos propios

    @Override
    public Optional<Category> findByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("El nombre no puede ser nulo");
        }
        return load().stream()
                .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Category> findByVideogameId(Long videogameId) {
        if (videogameId == null) {
            throw new IllegalArgumentException("El ID del videojuego no puede ser nulo");
        }

        File file = new File("data/videogames.json");
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        List<Long> categoryIds = new ArrayList<>();
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Videogame>>() {}.getType();
            List<Videogame> videogames = gson.fromJson(reader, listType);
            if (videogames != null) {
                for (Videogame v : videogames) {
                    if (videogameId.equals(v.getId()) && v.getCategoryIds() != null) {
                        categoryIds = v.getCategoryIds();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al leer data/videogames.json", e);
        }

        if (categoryIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> finalCategoryIds = categoryIds;
        return load().stream()
                .filter(c -> finalCategoryIds.contains(c.getId()))
                .toList();
    }

    //Métodos repetitivos

    private List<Category> load() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Category>>() {}.getType();
            List<Category> categories = gson.fromJson(reader, listType);
            return categories != null ? categories : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar las categorías desde " + FILE_PATH, e);
        }
    }

    private void saveToFile(List<Category> categories) {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(categories, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar las categorías en " + FILE_PATH, e);
        }
    }
}