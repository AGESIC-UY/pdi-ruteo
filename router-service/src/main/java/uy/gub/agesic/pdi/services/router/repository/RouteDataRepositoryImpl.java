package uy.gub.agesic.pdi.services.router.repository;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;
import uy.gub.agesic.pdi.common.exceptions.PDIException;
import uy.gub.agesic.pdi.common.git.GitManager;
import uy.gub.agesic.pdi.common.utiles.ListaUtil;
import uy.gub.agesic.pdi.common.utiles.StringAcceptor;
import uy.gub.agesic.pdi.common.utiles.dtos.ResultadoPaginadoDTO;
import uy.gub.agesic.pdi.services.router.domain.FiltroRutaDTO;
import uy.gub.agesic.pdi.services.router.domain.RutaDTO;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ManagedResource
public class RouteDataRepositoryImpl implements RouteDataRepository {

    private static Logger logger = LoggerFactory.getLogger(RouteDataRepositoryImpl.class);

    @Value("${application.git.fileRoutes}")
    private String fileRoutes;

    private GitManager gitManager;

    private Map<String,RutaDTO> mapRutas = new HashMap<>();

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public RouteDataRepositoryImpl(GitManager gitManager) {
        this.gitManager = gitManager;
    }

    @PostConstruct
    private void init() {
        boolean mustShutdown = false;

        try {
            // Refrescamos siempre el repositorio
            gitManager.recibirCambios();

            List<RutaDTO> rutas = mapper.readValue(new File(gitManager.getAbsoluteLocalPath(fileRoutes)), new TypeReference<List<RutaDTO>>() { });
            this.mapRutas.clear();

            for (RutaDTO r : rutas) {
                this.mapRutas.put(r.getLogical(),r);
            }
        } catch (PDIException e) {
            logger.error("Ha ocurrido un error inicializando el repositorio de rutas", e);
            mustShutdown = true;
        } catch (JsonParseException e) {
            logger.error("Ha ocurrido un error inicializando el repositorio de rutas", e);
            mustShutdown = true;
        } catch (JsonMappingException e) {
            logger.error("Ha ocurrido un error inicializando el repositorio de rutas", e);
            mustShutdown = true;
        } catch (IOException e) {
            logger.error("Ha ocurrido un error inicializando el repositorio de rutas", e);
            mustShutdown = true;
        }

        if (mustShutdown) {
            System.exit(10);
        }
    }

    @Override
    @ManagedOperation
    public void limpiarCache() {
        this.init();
    }

    @Override
    public ResultadoPaginadoDTO<RutaDTO> buscarRutas(FiltroRutaDTO filtro) throws PDIException {

        String logical = filtro.getLogical();
        String baseURI = filtro.getBaseURI();
        String physical = filtro.getPhysical();

        List<RutaDTO> listaRutas = new ArrayList<>(this.mapRutas.values());

        listaRutas = new ArrayList<>(this.mapRutas.values());

        listaRutas = ListaUtil.<RutaDTO, String>filterList("logical", logical, listaRutas, new StringAcceptor());
        listaRutas = ListaUtil.<RutaDTO, String>filterList("physical", physical, listaRutas, new StringAcceptor());
        listaRutas = ListaUtil.<RutaDTO, String>filterList("baseURI", baseURI, listaRutas, new StringAcceptor());

        long totalTuplas = listaRutas.size();

        Collections.sort(listaRutas);

        listaRutas = ListaUtil.<RutaDTO>getSubList(listaRutas, filtro.getCurrentPage(), filtro.getPageSize());

        ResultadoPaginadoDTO<RutaDTO> resultado = new ResultadoPaginadoDTO<>(listaRutas);
        resultado.setTotalTuplas(totalTuplas);

        return resultado;
    }

    @Override
    public RutaDTO obtenerRuta(String logical) throws PDIException  {
        return this.mapRutas.get(logical);
    }

    @Override
    public List<RutaDTO> obtenerTodasLasRutas() throws PDIException {
        List<RutaDTO> listaRutas = new ArrayList<>(this.mapRutas.values());

        return listaRutas;
    }

    @Override
    public Boolean existeRuta(String logical) throws PDIException {
        return this.mapRutas.containsKey(logical);
    }

    @Override
    public void agregarRuta(RutaDTO route, String logicalOld) throws PDIException {
        try {
            Map<String,RutaDTO> mapRutasTmp = new HashMap<>(this.mapRutas);
            if(logicalOld != null) {
                mapRutasTmp.remove(logicalOld);
            }
            mapRutasTmp.put(route.getLogical(),route);

            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(gitManager.getAbsoluteLocalPath(fileRoutes)), new ArrayList<>(mapRutasTmp.values()));

            this.gitManager.enviarCambios("Se inserta la ruta con nombre l\u00F3gico: " + route.getLogical(), fileRoutes);

            this.mapRutas = mapRutasTmp;
        } catch (PDIException e) {
            logger.error("Ha ocurrido un error insertando una nueva ruta: " + route.toString(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Ha ocurrido un error insertando una nueva ruta: " + route.toString(), e);
            throw new PDIException("Ha ocurrido un error insertando una nueva ruta: " + route.toString(), e);
        }
    }

    @Override
    public void eliminarRuta(String logical) throws PDIException {
        try {
            Map<String,RutaDTO> mapRutasTmp = new HashMap<>(this.mapRutas);
            mapRutasTmp.remove(logical);

            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(gitManager.getAbsoluteLocalPath(fileRoutes)), new ArrayList<>(mapRutasTmp.values()));

            this.gitManager.enviarCambios("Se elimina la ruta con nombre l\u00F3gico: " + logical, this.fileRoutes);

            this.mapRutas = mapRutasTmp;
        } catch (PDIException e) {
            logger.error("Ha ocurrido eliminando la ruta con nombre l\u00F3gico: " + logical, e);
            throw e;
        } catch (Exception e) {
            logger.error("Ha ocurrido eliminando la ruta con nombre l\u00F3gico: " + logical, e);
            throw new PDIException("Ha ocurrido eliminando la ruta con nombre l\u00F3gico: " + logical, e);
        }
    }

    public void clonarRepositorio() {
        try {
            this.gitManager.clonarRepositorio();
        } catch (PDIException e) {
            logger.error("Ha ocurrido un error clonando el repositorio");
        }
    }

    @ManagedOperation
    public String getRoutes() {
        StringBuffer buffer = new StringBuffer();
        for (RutaDTO route : this.mapRutas.values()) {
            buffer.append(route.toString());
            buffer.append(", \n");
        }
        return buffer.toString();
    }

}