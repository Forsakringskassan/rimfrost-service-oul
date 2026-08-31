package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import java.util.List;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.UppgiftDto;

public record UppgiftListResult(List<UppgiftDto>uppgifter,int borttagnaPgaBehorighet){public UppgiftListResult{uppgifter=List.copyOf(uppgifter);}}
