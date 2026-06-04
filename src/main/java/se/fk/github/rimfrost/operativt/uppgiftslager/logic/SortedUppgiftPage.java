package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import java.util.List;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.UppgiftDto;

public record SortedUppgiftPage(int total,List<UppgiftDto>items){}
