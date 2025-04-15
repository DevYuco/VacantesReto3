package vacantes.modelo.service;

import org.springframework.stereotype.Service;

import vacantes.modelo.entities.Empresa;
import vacantes.modelo.entities.Usuario;
import vacantes.repository.IGenericoCRUD;

@Service
public interface EmpresaService extends IGenericoCRUD<Empresa, Integer>{
	Empresa buscarEmpresaPorEmail(String email);
	Empresa buscarEmpresaPorId(int idEmpresa);
	Empresa buscarEmpresaPorUsuario(Usuario usuario);
	}
