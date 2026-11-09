package es.dam.di.repository.file;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import es.dam.di.model.Review;
import es.dam.di.repository.IReviewRepository;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReviewRepositoryFile implements IReviewRepository {

    private static final String FILE_PATH = "data/reviews.json";
    private final Gson gson = GsonManager.getGson();

    @Override
    public <S extends Review> S save(S entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        List<Review> reviews = load();

        if (entity.getId() == null) {
            long newId = reviews.stream()
                    .mapToLong(Review::getId)
                    .max()
                    .orElse(0L) + 1;
            entity.setId(newId);
            reviews.add(entity);
        } else {
            int index = -1;
            for (int i = 0; i < reviews.size(); i++) {
                if (reviews.get(i).getId().equals(entity.getId())) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                reviews.set(index, entity);
            } else {
                reviews.add(entity);
            }
        }

        saveToFile(reviews);
        return entity;
    }

    @Override
    public Review findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        return load().stream()
                .filter(r -> id.equals(r.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Optional<Review> findByIdOptional(Long id) {
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
    public List<Review> findAllToList() {
        return load();
    }

    @Override
    public Iterable<Review> findAll() {
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
        List<Review> reviews = load();
        boolean eliminado = reviews.removeIf(r -> id.equals(r.getId()));
        if (eliminado) {
            saveToFile(reviews);
        }
    }

    @Override
    public void deleteAll() {
        saveToFile(new ArrayList<>());
    }

    //Métodos propios

    @Override
    public List<Review> findByVideogameId(Long videogameId) {
        if (videogameId == null) {
            throw new IllegalArgumentException("El ID del videojuego no puede ser nulo");
        }
        return load().stream()
                .filter(r -> videogameId.equals(r.getVideogameId()))
                .toList();
    }

    @Override
    public double getRecommendationPercentageByVideogameId(Long videogameId) {
        if (videogameId == null) {
            throw new IllegalArgumentException("El ID del videojuego no puede ser nulo");
        }

        List<Review> gameReviews = findByVideogameId(videogameId);
        if (gameReviews.isEmpty()) {
            return 0.0;
        }

        // contamos cuantas recomendaciones hay positivas
        long recommendedCount = gameReviews.stream()
                .filter(Review::isRecommended)
                .count();

        return (recommendedCount * 100.0) / gameReviews.size();
    }

    //Métodos repetitivos

    private List<Review> load() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Review>>() {}.getType();
            List<Review> reviews = gson.fromJson(reader, listType);
            return reviews != null ? reviews : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar las reseñas desde " + FILE_PATH, e);
        }
    }

    private void saveToFile(List<Review> reviews) {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(reviews, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar las reseñas en " + FILE_PATH, e);
        }
    }
}