package vacantes.modelo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vacantes.modelo.entities.Solicitud;
import vacantes.repository.IGenericoCRUD;

@Service
public interface SolicitudService extends IGenericoCRUD<Solicitud, Integer>{
	
	Solicitud asignarSolicitud(int idSolicitud);
	List<Solicitud> buscarSolicitudPorIdUsuario(String email);
	List<Solicitud> buscarSolicitudPorIdVacante(int idVacante);
	public List<Solicitud> buscarSolicitudPorEmpresa(int idEmpresa);
	Solicitud buscarSolicitudPorIdVacanteYEmail(int idVacante, String email);
}
