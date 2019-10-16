package io.github.erdos.bellang.reader;

import io.github.erdos.bellang.objects.Character;

import java.io.EOFException;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.HashMap;
import java.util.Map;

import static io.github.erdos.bellang.reader.Reader.expectCharacter;
import static io.github.erdos.bellang.reader.SymbolReader.readUntilDelimiter;

final class CharacterReader {

	private CharacterReader() {}

	static final Map<String, Character> charactersByName = new HashMap<String, Character>() {{
		put("bel", Character.character((char) 7));
		put("tab", Character.character((char) 9));
		put("lf", Character.character((char) 10));
		put("cr", Character.character((char) 13));
		put("sp", Character.character((char) 31)); // space
	}};


	static Character readCharacter(PushbackReader pbr) throws IOException {
		if (expectCharacter(pbr, '\\')) {
			int read = pbr.read();
			if (read == -1) {
				throw new EOFException("EOF while reading character. There is a '\\' at the end of the file!");
			} else {
				String wordTail;
				try {
					wordTail = readUntilDelimiter(pbr);
				} catch (EOFException e) {
					wordTail = null;
				}

				if (wordTail == null) {
					return new Character((char) read);
				} else {
					final String word = ((char) read) + wordTail;
					final Character result = charactersByName.get(word);
					if (result == null) {
						throw new IllegalArgumentException("Could not find character with name" + word);
					} else {
						return result;
					}
				}
			}
		} else {
			return null;
		}
	}
}
