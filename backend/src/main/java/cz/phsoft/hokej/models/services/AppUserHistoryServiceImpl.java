package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.repositories.AppUserHistoryRepository;
import cz.phsoft.hokej.models.dto.AppUserHistoryDTO;
import cz.phsoft.hokej.models.mappers.AppUserHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementace servisní vrstvy pro práci s historií uživatelských účtů.
 *
 * Zajišťuje načítání historických záznamů uživatele z databáze
 * prostřednictvím repozitáře a jejich převod na DTO objekty
 * pomocí mapperu.
 *
 * Třída neprovádí žádné zápisy do databáze. Historické záznamy
 * jsou vytvářeny databázovými triggery a tato služba slouží
 * výhradně pro čtecí a auditní účely.
 */
@Service
public class AppUserHistoryServiceImpl implements AppUserHistoryService {

    private final AppUserHistoryRepository repository;
    private final AppUserHistoryMapper mapper;

    /**
     * Vytvoří instanci servisní třídy.
     *
     * @param repository repozitář pro přístup k historickým záznamům uživatelů
     * @param mapper mapper pro převod entit na DTO objekty
     */
    public AppUserHistoryServiceImpl(
            AppUserHistoryRepository repository,
            AppUserHistoryMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Vrátí historii změn uživatele podle jeho e-mailové adresy.
     *
     * Záznamy jsou načteny z databáze v sestupném pořadí
     * podle času změny a následně převedeny na DTO objekty.
     *
     * @param email e-mailová adresa uživatele
     * @return seznam historických záznamů uživatele
     */
    @Override
    public List<AppUserHistoryDTO> getHistoryForUser(String email) {
        return mapper.toDTOList(
                repository.findByEmailOrderByChangedAtDesc(email)
        );
    }

    /**
     * Vrátí historii změn uživatele podle jeho identifikátoru.
     *
     * Záznamy jsou načteny z databáze v sestupném pořadí
     * podle času změny a následně převedeny na DTO objekty.
     *
     * @param id identifikátor uživatele
     * @return seznam historických záznamů uživatele
     */
    @Override
    public List<AppUserHistoryDTO> getHistoryForUser(Long id) {
        return mapper.toDTOList(
                repository.findByUserIdOrderByChangedAtDesc(id)
        );
    }
}
