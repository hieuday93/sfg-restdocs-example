package guru.springframework.sfgrestdocsexample.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.sfgrestdocsexample.domain.Beer;
import guru.springframework.sfgrestdocsexample.repositories.BeerRepository;
import guru.springframework.sfgrestdocsexample.web.model.BeerDto;
import guru.springframework.sfgrestdocsexample.web.model.BeerStyleEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs
@WebMvcTest(BeerController.class)
@ComponentScan(basePackages = "guru.springframework.sfgrestdocsexample.web.mappers")
class BeerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BeerRepository beerRepository;

    @Test
    void getBeerById() throws Exception {
        given(beerRepository.findById(any())).willReturn(Optional.of(getValidBeer()));

        mockMvc.perform(get("/api/v1/beer/{beerId}", UUID.randomUUID())
                        .param("isCold", "yes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("v1/beer",
                        pathParameters(
                                parameterWithName("beerId").description("UUID of desired beer to get")
                        ),
                        queryParameters(
                                parameterWithName("isCold").description("Is Beer Cold Query param")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.STRING).description("Id of Beer"),
                                fieldWithPath("version").type(JsonFieldType.NUMBER).description("Version number"),
                                fieldWithPath("createdDate").type(JsonFieldType.STRING).description("Date created"),
                                fieldWithPath("lastModifiedDate").type(JsonFieldType.STRING).description("Date updated"),
                                fieldWithPath("beerName").optional().type(JsonFieldType.STRING).description("Beer name"),
                                fieldWithPath("beerStyle").optional().type(JsonFieldType.STRING).description("Beer style"),
                                fieldWithPath("upc").type(JsonFieldType.NUMBER).description("UPC of Beer"),
                                fieldWithPath("price").optional().type(JsonFieldType.NUMBER).description("Price"),
                                fieldWithPath("quantityOnHand").optional().type(JsonFieldType.NUMBER).description("Quantity on Hand")
                        )
                ));
    }

    @Test
    void saveNewBeer() throws Exception {
        BeerDto beerDto =  getValidBeerDto();
        String beerDtoJson = objectMapper.writeValueAsString(beerDto);

        ConstrainedFields fields = new ConstrainedFields(BeerDto.class);

        mockMvc.perform(post("/api/v1/beer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(beerDtoJson))
                .andExpect(status().isCreated())
                .andDo(document("v1/beer",
                        requestFields(
                                fields.withPath("id").ignored().description("Beer ID - will be created and returned by the server"),
                                fields.withPath("version").ignored().description("Version number - will be created and returned by the server"),
                                fields.withPath("createdDate").ignored().description("Date created - will be created and returned by the server"),
                                fields.withPath("lastModifiedDate").ignored().description("Date updated - will be created and returned by the server"),
                                fields.withPath("beerName").type(JsonFieldType.STRING).description("Beer name"),
                                fields.withPath("beerStyle").type(JsonFieldType.STRING).description("Beer style"),
                                fields.withPath("price").type(JsonFieldType.NUMBER).description("Beer Price"),
                                fields.withPath("upc").type(JsonFieldType.NUMBER).description("UPC number"),
                                fields.withPath("quantityOnHand").ignored().description("Quantity on Hand - leaving null to process later")
                        )));
    }

    @Test
    void updateBeerById() throws Exception {
        BeerDto beerDto =  getValidBeerDto();
        String beerDtoJson = objectMapper.writeValueAsString(beerDto);

        mockMvc.perform(put("/api/v1/beer/" + UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(beerDtoJson))
                .andExpect(status().isNoContent());
    }

    BeerDto getValidBeerDto(){
        return BeerDto.builder()
                .beerName("Nice Ale")
                .beerStyle(BeerStyleEnum.ALE)
                .price(new BigDecimal("9.99"))
                .upc(123123123123L)
                .build();

    }

    Beer getValidBeer() {
        return Beer.builder()
                .id(UUID.randomUUID())
                .version(1L)
                .createdDate(Timestamp.valueOf(LocalDateTime.now()))
                .lastModifiedDate(Timestamp.valueOf(LocalDateTime.now()))
                .beerName("Nice Ale")
                .beerStyle("ALE")
                .upc(123L)
                .price(BigDecimal.valueOf(100000.0))
                .minOnHand(10)
                .quantityToBrew(10)
                .build();
    }

    private static class ConstrainedFields {
        private final ConstraintDescriptions constraintDescriptions;

        ConstrainedFields(Class<?> input) {
            this.constraintDescriptions = new ConstraintDescriptions(input);
        }

        private FieldDescriptor withPath(String path) {
            return fieldWithPath(path).attributes(new Attributes.Attribute(
                    "constraints",
                    StringUtils.collectionToDelimitedString(
                            this.constraintDescriptions.descriptionsForProperty(path),
                            ". ")));
        }
    }

}