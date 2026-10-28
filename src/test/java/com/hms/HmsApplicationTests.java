package com.hms;

import com.hms.mock.MockDataStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class HmsApplicationTests {

    @Autowired
    private MockDataStore dataStore;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        assertThat(dataStore).isNotNull();
        assertThat(dataStore.examinations).isNotEmpty();
        assertThat(dataStore.patients).isNotEmpty();
        assertThat(dataStore.rooms).isNotEmpty();
        assertThat(dataStore.staffMembers).isNotEmpty();
        assertThat(dataStore.medicines).isNotEmpty();
        assertThat(dataStore.invoices).isNotEmpty();
    }

    @Test
    void testRootUrlReturnsRoleSelectView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/role-select"));
    }

    @Test
    void testDataLinkingIntegrity() {
        // Ensure resetData() and linkAll() execute cleanly without throwing any NPE or IllegalStateException
        dataStore.resetData();
        assertThat(dataStore.examinations.get("PKM001").getPatientName()).isEqualTo("Nguyễn Minh Anh");
        assertThat(dataStore.examinations.get("PKM003").getDiagnosisName()).isEqualTo("Chưa có chẩn đoán");
        assertThat(dataStore.examinations.get("PKM003").getPatientName()).isEqualTo("Lê Thu Hà");
    }
}

