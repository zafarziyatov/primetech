package uz.zafar.primetech.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.zafar.primetech.db.service.UserService;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService ;
    @GetMapping("/users/{page}")
    public ResponseEntity getUsers(@PathVariable int page){
        return ResponseEntity.ok(userService.getAll(page,2));
    }
}
