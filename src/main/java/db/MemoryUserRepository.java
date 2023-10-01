package db;

import model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryUserRepository implements Repository {
    private final Map<String, User> users = new HashMap<>();
    private static MemoryUserRepository memoryUserRepository;

    private MemoryUserRepository() {
    }

    // 싱글톤 (DB로 쓰기 위함)
    public static MemoryUserRepository getInstance() {
        if (memoryUserRepository == null) {
            memoryUserRepository = new MemoryUserRepository();
            return memoryUserRepository;
        }
        return memoryUserRepository;
    }

    @Override
    public void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    @Override
    public User findUserById(String userId) {
        return users.get(userId);
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }
}
