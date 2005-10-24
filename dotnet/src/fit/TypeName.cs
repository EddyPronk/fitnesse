// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System.Text;
using System.Text.RegularExpressions;

namespace fit
{
	public class TypeName
	{
		private string name;
		private string[] parts;

		public TypeName(string name)
		{
			this.name = name;
			parts = name.Split('.');
		}

		public string Name
		{
			get { return parts[parts.Length - 1]; }
		}

		public string Namespace
		{
			get
			{
				StringBuilder builder = new StringBuilder(parts[0]);
				for (int i = 1; i < parts.Length - 1; i++)
				{
					builder.Append(".");
					builder.Append(parts[i]);
				}
				return builder.ToString();
			}
		}

		public bool IsFullyQualified()
		{
			return Regex.IsMatch(name, "^([A-Za-z0-9_]+\\.)+[A-Za-z0-9_]+$");
		}

		public string OriginalName
		{
			get { return name; }
		}

	}
}