package org.springframework.scripting.jruby;

/**
 * http://opensource.atlassian.com/projects/spring/browse/SPR-3038
 *
 * @author Rick Evans
 */
public interface WrapperAdder {

	Integer addInts(Integer x, Integer y);

	Short addShorts(Short x, Short y);

	Long addLongs(Long x, Long y);

	Float addFloats(Float x, Float y);

	Double addDoubles(Double x, Double y);

	Boolean resultIsPositive(Integer x, Integer y);

	String concatenate(Character c, Character d);

	Character echo(Character c);

	String concatArrayOfIntegerWrappers(Integer[] numbers);

	Short[] populate(Short one, Short two);
}
