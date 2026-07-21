package com.nimbuscart.user_service.Service;


import com.nimbuscart.user_service.Model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {
    private final List<User> users = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong();
    public User createUser(User user) {
        user.setId(idCounter.incrementAndGet());
        users.add(user);
        return user;
    }
    public List<User> getAllUsers() {
        return users;
    }
    public User getUserById(Long id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }



}
