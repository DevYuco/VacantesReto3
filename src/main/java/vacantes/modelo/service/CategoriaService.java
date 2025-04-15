package vacantes.modelo.service;

import org.springframework.stereotype.Service;

import vacantes.modelo.entities.Categoria;
import vacantes.repository.IGenericoCRUD;

@Service
public interface CategoriaService extends IGenericoCRUD<Categoria, Integer>{

}
