package com.usuarios.demo.controller;

import com.usuarios.demo.controllers.AdminController;
import com.usuarios.demo.entities.Admin;
import com.usuarios.demo.services.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import com.usuarios.demo.config.TestSecurityConfig;



import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;

@WebMvcTest(controllers = AdminController.class)
@Import(TestSecurityConfig.class) 
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Test
    @DisplayName("✅ Debería retornar todos los administradores correctamente")
    void obtenerTodosAdmins() throws Exception {
        Admin admin1 = new Admin("admin1", "Admin Uno");
        Admin admin2 = new Admin("admin2", "Admin Dos");
        List<Admin> mockAdmins = List.of(admin1, admin2);

        when(adminService.obtenerTodos()).thenReturn(mockAdmins);

        mockMvc.perform(get("/api/admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].pkId").value("admin1"))
                .andExpect(jsonPath("$[0].nombreCompleto").value("Admin Uno"))
                .andExpect(jsonPath("$[1].pkId").value("admin2"))
                .andExpect(jsonPath("$[1].nombreCompleto").value("Admin Dos"));

        verify(adminService, times(1)).obtenerTodos();
    }
}
