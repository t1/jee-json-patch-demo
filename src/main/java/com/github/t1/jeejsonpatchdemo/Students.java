package com.github.t1.jeejsonpatchdemo;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.t1.jeejsonpatchdemo.Gender.female;
import static com.github.t1.jeejsonpatchdemo.Gender.male;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_PATCH_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_MERGE_PATCH_JSON;

@Path("/students")
@Singleton
@Slf4j
public class Students {
    private final Map<Long, Student> students = new ConcurrentHashMap<>();

    @POST @Path("/reset")
    @PostConstruct public void reset() {
        log.debug("reset students");
        students.put(1L, new Student(1L, "Alice", "Doe", female, "MiddleWood School"));
        students.put(2L, new Student(2L, "Bob", "Doe", male, "MiddleWood School"));
    }

    @GET @Path("/{id}")
    @Produces(APPLICATION_JSON)
    public Student get(@PathParam("id") Long id) {
        var student = students.get(id);
        log.debug("get student {}: {}", id, student);
        return Optional.ofNullable(student).orElseThrow(() -> new NotFoundException("student " + id));
    }

    @PATCH
    @Path("/{id}")
    @Consumes(APPLICATION_JSON_PATCH_JSON)
    public Student patchStudent(@PathParam("id") long id, Student student) {
        log.debug("patch student {}: {}", id, student);
        if (!students.containsKey(id)) throw new NotFoundException();
        students.put(id, student);
        return student;
    }

    @PATCH
    @Path("/{id}")
    @Consumes(APPLICATION_MERGE_PATCH_JSON)
    public Student mergePatchStudent(@PathParam("id") long id, Student student) {
        log.debug("merge patch student {}: {}", id, student);
        if (!students.containsKey(id)) throw new NotFoundException();
        students.put(id, student);
        return student;
    }
}
