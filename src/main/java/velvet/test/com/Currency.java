package velvet.test.com;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {
    USD(431), EUR(451);

    private final int id;
}
